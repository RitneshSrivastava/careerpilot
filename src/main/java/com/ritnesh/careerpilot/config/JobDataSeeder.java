package com.ritnesh.careerpilot.config;

import com.ritnesh.careerpilot.entity.JobListing;
import com.ritnesh.careerpilot.repository.JobListingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds a handful of sample job listings on first startup so the
 * job-matching feature has real data to match against, without needing
 * a live external job-board API.
 */
@Component
public class JobDataSeeder implements CommandLineRunner {

    private final JobListingRepository jobListingRepository;

    public JobDataSeeder(JobListingRepository jobListingRepository) {
        this.jobListingRepository = jobListingRepository;
    }

    @Override
    public void run(String... args) {

        if (jobListingRepository.count() > 0) {
            return; // already seeded
        }

        List<JobListing> jobs = List.of(
                job("Backend Engineer", "Nimbus Systems", "Bengaluru, IN",
                        "Build and maintain REST APIs for our core platform using Java and Spring Boot.",
                        "Java,Spring Boot,Spring Security,MySQL,REST APIs,Git,Hibernate"),

                job("AI Application Engineer", "Vertex Labs", "Remote",
                        "Integrate LLM-powered features into production backend services.",
                        "Python,AI/ML,GenAI,RAG,Prompt Engineering,REST APIs,Java"),

                job("Full Stack Developer", "Bramble Tech", "Pune, IN",
                        "Own features end-to-end across a Spring Boot backend and React frontend.",
                        "Java,Spring Boot,React,MySQL,REST APIs,Git"),

                job("Software Engineer, Platform", "Ironclad Systems", "Hyderabad, IN",
                        "Work on core platform infrastructure and internal tooling.",
                        "Java,C++,Data Structures and Algorithms,Linux,Git"),

                job("Junior Backend Developer", "Fieldstone Software", "Gurgaon, IN",
                        "Entry-level role building and testing backend services.",
                        "Java,Spring Boot,MySQL,REST APIs,Postman,GitHub"),

                job("Machine Learning Engineer (Entry-Level)", "Solace AI", "Remote",
                        "Support development of ML-powered features across the product.",
                        "Python,AI/ML,GenAI,SQL,Prompt Engineering")
        );

        jobListingRepository.saveAll(jobs);
    }

    private JobListing job(String title, String company, String location,
                            String description, String requiredSkillsCsv) {
        JobListing j = new JobListing();
        j.setTitle(title);
        j.setCompany(company);
        j.setLocation(location);
        j.setDescription(description);
        j.setRequiredSkillsCsv(requiredSkillsCsv);
        j.setPostedAt(LocalDateTime.now());
        return j;
    }
}
