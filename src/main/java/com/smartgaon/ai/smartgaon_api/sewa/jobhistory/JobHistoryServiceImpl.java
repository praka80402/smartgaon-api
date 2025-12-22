package com.smartgaon.ai.smartgaon_api.sewa.jobhistory;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.sewa.jobs.Job;
import com.smartgaon.ai.smartgaon_api.sewa.jobs.JobApplication;
import com.smartgaon.ai.smartgaon_api.sewa.jobs.JobApplicationRepository;
import com.smartgaon.ai.smartgaon_api.sewa.jobs.JobRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class JobHistoryServiceImpl implements JobHistoryService {

    private final JobApplicationRepository applicationRepo;
    private final JobReviewRepository reviewRepo;
    private final JobRepository jobRepo;
    private final UserRepository userRepo;


    @Override
public Page<EmployerJobHistoryResponse> getEmployerJobHistory(
        Long employerId, int page, int size) {

    Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("appliedAt").descending()
    );

    Page<JobApplication> acceptedApps =
            applicationRepo.findByEmployerIdAndStatus(
                    employerId,
                    "ACCEPTED",
                    pageable
            );

    return acceptedApps.map(app -> {

        Job job = jobRepo.findById(app.getJobId())
                .orElseThrow();

        User applicant = userRepo.findById(app.getApplicantId())
                .orElseThrow();

        JobReview review = reviewRepo
                .findByJobIdAndApplicantId(
                        app.getJobId(),
                        app.getApplicantId()
                )
                .orElse(null);

        return new EmployerJobHistoryResponse(
                job.getId(),
                job.getTitle(),
                applicant.getId(),
                applicant.getFirstName() + " " + applicant.getLastName(),
                applicant.getProfileImageUrl(),
                review != null ? review.getRating() : null,
                review != null ? review.getComment() : null,
                app.getAppliedAt()
        );
    });
}


//     @Override
//     public List<EmployerJobHistoryResponse>
//     getEmployerJobHistory(Long employerId) {

//         List<JobApplication> acceptedApps =
//                 applicationRepo.findByEmployerIdAndStatus(
//                         employerId, "ACCEPTED"
//                 );
//         System.out.println("acceptedApps"+acceptedApps.size());

//         return acceptedApps.stream().map(app -> {


//             System.out.println("app.getJobId()"+app.getJobId());
//             System.out.println("app.getJobId()"+app.getApplicantId());

//             Job job = jobRepo.findById(app.getJobId())
//                     .orElseThrow();

//             User applicant = userRepo.findById(app.getApplicantId())
//                     .orElseThrow();

//             JobReview review =
//                     reviewRepo
//                             .findByJobIdAndApplicantId(
//                                     app.getJobId(),
//                                     app.getApplicantId()
//                             )
//                             .orElse(null);

//             System.out.println("applicant"
//                     +applicant.getFirstName());

//             return new EmployerJobHistoryResponse(
//                     job.getId(),
//                     job.getTitle(),
//                     applicant.getId(),
//                     applicant.getFirstName() + " " +
//                             applicant.getLastName(),
//                     applicant.getProfileImageUrl(),
//                     review != null ? review.getRating() : null,
//                     review != null ? review.getComment() : null,
//                     app.getAppliedAt()
//             );
//         }).toList();
//     }

    @Override
    public void submitReview(
            Long jobId,
            Long applicantId,
            Integer rating,
            String comment
    ) {

        JobApplication app =
                applicationRepo
                        .findByJobIdAndApplicantId(jobId, applicantId)
                        .orElseThrow(() ->
                                new RuntimeException("Application not found"));

        if (!"ACCEPTED".equals(app.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only accepted candidates can be reviewed"
            );

        }

        if (reviewRepo.existsByJobIdAndApplicantId(
                jobId, applicantId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Review already submitted"
                );

        }


        JobReview review = new JobReview();
        review.setJobId(jobId);
        review.setApplicantId(applicantId);
        review.setEmployerId(app.getEmployerId());
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        reviewRepo.save(review);
    }
}
