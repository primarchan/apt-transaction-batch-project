package com.example.housebatch.job.apt;

import com.example.housebatch.core.dto.AptDealDto;
import com.example.housebatch.job.validator.FilePathParameterValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AptDealInsertJobConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job aptDealInsertJob(Step aptDealInsertStep) {
        return jobBuilderFactory.get("aptDealInsertJob")
                .incrementer(new RunIdIncrementer())
                .validator(new FilePathParameterValidator())
                .start(aptDealInsertStep)
                .build();
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
            @Value("#{jobParameters['filePath']}") String filePath,
            Jaxb2Marshaller aptDealDtoMarshaller
    ) {
        return new StaxEventItemReaderBuilder<AptDealDto>().name("aptDealResourceReader")
                .resource(new ClassPathResource(filePath))
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
