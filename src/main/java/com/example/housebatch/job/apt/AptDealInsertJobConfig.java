package com.example.housebatch.job.apt;

import com.example.housebatch.Adapter.ApartmentApiResource;
import com.example.housebatch.core.dto.AptDealDto;
import com.example.housebatch.core.repository.LawdRepository;
import com.example.housebatch.job.validator.YearMonthParameterValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AptDealInsertJobConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final ApartmentApiResource apartmentApiResource;

    private final LawdRepository lawdRepository;

    @Bean
    public Job aptDealInsertJob(
            Step guLawdCdStep,
            Step contextPrintStep
//            Step aptDealInsertStep
    ) {
        return jobBuilderFactory.get("aptDealInsertJob")
                .incrementer(new RunIdIncrementer())
                .validator(aptDealJobParameterValidator())
                .start(guLawdCdStep)
                .on("CONTINUABLE").to(contextPrintStep).next(guLawdCdStep)
                .from(guLawdCdStep)
                .on("*").end()
                .end()
                .build();
    }

    private JobParametersValidator aptDealJobParameterValidator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(Arrays.asList(
                new YearMonthParameterValidator()
        ));

        return validator;
    }

    @Bean
    @JobScope
    public Step guLawdCdStep(Tasklet guLawdCdTasklet) {
        return stepBuilderFactory.get("guLawdCdStep")
                .tasklet(guLawdCdTasklet)
                .build();
    }

    /**
     * ExecutionContext 에 저장할 데이터
     * 1. guLawdCd - 구 코드 -> 다음 Step 에서 활용할 값
     * 2. guLawdCdList - 구 코드 리스트
     * 3. itemCount - 남아있는 구 코드의 갯수
     */
    @Bean
    @StepScope
    public Tasklet guLawdCdTasklet() {
        return (contribution, chunkContext) -> {
            StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
            ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();

            /**
             * 데이터가 존재하면 다음 스텝을 실행, 데이터가 존재하지 않으면 종료
             * 데이터가 존재 -> CONTINUABLE
             */
            List<String> guLawdCdList;
            if (!executionContext.containsKey("guLawdCdList")) {
                guLawdCdList = lawdRepository.findDistinctGuLawdCd();
                executionContext.put("guLawdCdList", guLawdCdList);
                executionContext.putInt("itemCount", guLawdCdList.size());
            } else {
                guLawdCdList = (List<String>) executionContext.get("guLawdCdList");
            }

            Integer itemCount = executionContext.getInt("itemCount");

            if (itemCount == 0) {
                contribution.setExitStatus(ExitStatus.COMPLETED);
                return RepeatStatus.FINISHED;
            }

            itemCount--;

            String guLawdCd = guLawdCdList.get(itemCount);
            executionContext.putString("guLawdCd", guLawdCd);
            executionContext.putInt("itemCount", itemCount);

            contribution.setExitStatus(new ExitStatus("CONTINUABLE"));

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    @JobScope
    public Step contextPrintStep(Tasklet contextPrintTasklet) {
        return stepBuilderFactory.get("contextPrintStep")
                .tasklet(contextPrintTasklet)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet contextPrintTasklet(
            @Value("#{jobExecutionContext['guLawdCd']}") String guLawdCd
    ) {
        return (contribution, chunkContext) -> {
            System.out.println("[contextPrintTasklet] guLawdCd = " + guLawdCd);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    @JobScope
    public Step aptDealInsertStep(
            StaxEventItemReader<AptDealDto> aptDealResourceReader,
            ItemWriter<AptDealDto> aptDealItemWriter
    ) {
        return stepBuilderFactory.get("aptDealInsertStep")
                .<AptDealDto, AptDealDto>chunk(10)
                .reader(aptDealResourceReader)
                .writer(aptDealItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemReader<AptDealDto> aptDealResourceReader(
            @Value("#{jobExecutionContext['guLawdCd']}") String guLawdCd,
            @Value("#{jobParameters['yearMonth']}") String yearMonth,
            Jaxb2Marshaller aptDealDtoMarshaller
    ) {
        return new StaxEventItemReaderBuilder<AptDealDto>().name("aptDealResourceReader")
                .resource(apartmentApiResource.getResource(guLawdCd, YearMonth.parse(yearMonth)))
                .addFragmentRootElements("item")
                .unmarshaller(aptDealDtoMarshaller)
                .build();
    }

    @Bean
    @StepScope
    public Jaxb2Marshaller aptDealDtoMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(AptDealDto.class);

        return jaxb2Marshaller;
    }

    @Bean
    @StepScope
    public ItemWriter<AptDealDto> aptDealItemWriter() {
        return items -> {
          items.forEach(System.out::println);
        };
    }

}
