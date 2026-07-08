# Web Backend – Shiksha Quiz (user-facing)

Base package: com.smartgaon.ai.smartgaon_api.shikshaquiz
Base path:    /api/shiksha-quiz   (old Groq quiz keeps /api/quiz — no conflict)

## Where to put it
Copy the whole `shikshaquiz/` folder into:
  src/main/java/com/smartgaon/ai/smartgaon_api/shikshaquiz/

## What's inside
- model/, repository/, dto/, util/  → same as admin (only package renamed)
- service/QuizService, PerformanceService, QuizRedisService  → user-facing logic
- service/RotationSchedulerService   → cron REMOVED (reads active batch only)
- controller/ShikshaQuizController    → NEW, uses JWT email → userId

## Endpoints (all need a logged-in user's Bearer token)
- POST /api/shiksha-quiz/start?segmentKey=SSC
- POST /api/shiksha-quiz/attempt/{attemptId}/extra-time
- POST /api/shiksha-quiz/submit            (body: {attemptId, answers:[{questionId, selectedOption}]})
- GET  /api/shiksha-quiz/performance
- GET  /api/shiksha-quiz/rotation/countdown

## How userId works (the key difference from admin)
Admin used ?userId=1. Web resolves it from the JWT:
  JWT subject = email  →  userRepository.findByEmail(email)  →  user.getId()
No userId is ever taken from the request.

## IMPORTANT — no scheduling on web
- The rotation cron (@Scheduled) exists ONLY in the admin backend.
- Web has no @EnableScheduling (checked: main class is clean), and the cron
  method was removed from RotationSchedulerService here.
- Batches are rotated by admin; web just reads the active batch.

## Same DB
Table names are identical (quiz_question, quiz_attempt, quiz_question_batch,
quiz_competition_config, quiz_attempt_answer). Web and admin share the same
tables — questions/batches created in admin are immediately usable here.

## Redis
QuizRedisService needs Redis (attempt-limit counter + active-batch cache).
If web already has spring-boot-starter-data-redis + config, it just works.
If not, add the dependency and redis host/port (falls back to DB if absent).

## Dependencies to confirm in web pom.xml
- spring-boot-starter-data-redis  (for QuizRedisService)
- spring-boot-starter-validation  (if DTOs use validation annotations)

## Old Groq quiz
Left untouched at /api/quiz. When this new quiz goes live, ask the team to
remove the old /api/quiz/groq-key/raw endpoint and revoke that key (security).
