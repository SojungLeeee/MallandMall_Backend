package com.exam.answer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exam.question.Question;
import com.exam.question.QuestionDTO;
import com.exam.question.QuestionRepository;
import com.exam.user.Role;
import com.exam.user.User;
import com.exam.user.UserRepository;

@Service
public class AnswerServiceImpl implements AnswerService {

	private final AnswerRepository answerRepository;
	private final QuestionRepository questionRepository;
	private final UserRepository userRepository;

	// 생성자 주입
	public AnswerServiceImpl(AnswerRepository answerRepository,
		QuestionRepository questionRepository,
		UserRepository userRepository) {
		this.answerRepository = answerRepository;
		this.questionRepository = questionRepository;
		this.userRepository = userRepository;
	}

	// 답변 추가
	@Transactional
	@Override
	public AnswerDTO addAnswer(Long questionId, String userId, String content, String status) {
		// 질문이 존재하는지 확인
		Question question = questionRepository.findById(questionId)
			.orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));

		// 사용자가 존재하는지 확인
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		// 관리자 권한 확인 (관리자만 답변 가능)
		if (!Role.fromString(String.valueOf(user.getRole())).equals(Role.ADMIN)) {  // Role에서 문자열을 enum으로 변환하여 비교
			throw new RuntimeException("관리자만 답변을 작성할 수 있습니다.");
		}
		// 기본값으로 'ACTIVE' 설정 또는 클라이언트로부터 받은 값을 설정
		String answerStatus = (status != null) ? status : "ACTIVE";  // status가 null이면 'ACTIVE'로 기본 설정

		// 답변 생성
		Answer answer = Answer.builder()
			.questionId(questionId)  // 실제 질문 ID를 설정
			.content(content)         // 답변 내용
			.userId(userId)
			.status(answerStatus)  // status 추가
			.build();

		// 질문 상태 업데이트 (status를 'ANSWERED'로 설정)
		Optional<Question> questionOptional = questionRepository.findById(questionId);
		if (questionOptional.isPresent()) {
			Question question2 = questionOptional.get();
			question2.setStatus(Question.QuestionStatus.ANSWERED); // 상태를 'ANSWERED'로 변경
			questionRepository.save(question2); // 변경된 상태 저장
		}

		// 답변 저장
		answerRepository.save(answer);
		return null;
	}

	// 답변 수정
	@Override
	@Transactional
	public AnswerDTO updateAnswer(Long answerId, String userId, String content) {
		// 답변이 존재하는지 확인
		Answer answer = answerRepository.findById(answerId)
			.orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));

		// 관리자가 해당 답변을 수정할 권한이 있는지 확인
		if (!answer.getUserId().equals(userId)) {
			throw new RuntimeException("본인만 수정할 수 있습니다.");
		}

		// 답변 내용 수정
		answer.setContent(content);

		// 수정된 답변 저장
		answerRepository.save(answer);
		return null;
	}

	@Override
	public List<QuestionDTO> getAllQuestions() {
		// 모든 질문을 가져와서 DTO로 변환 후 반환
		return questionRepository.findAll().stream()
			.map(question -> new QuestionDTO(
				question.getQuestionid(),  // questionid
				question.getUserId(),      // userId
				question.getTitle(),       // title
				question.getContent(),     // content
				question.getCreateDate(),  // createDate
				question.getStatus().name()))  // status (enum을 문자열로 변환)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void deleteAnswer(Long answerId, String userId) {
		// 답변이 존재하는지 확인
		Answer answer = answerRepository.findById(answerId)
			.orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));

		// 사용자가 해당 답변을 삭제할 권한이 있는지 확인
		if (!answer.getUserId().equals(userId)) {
			throw new RuntimeException("본인만 삭제할 수 있습니다.");
		}

		// 질문 ID 가져오기 (Answer 객체에서 questionId를 가져오는 방식)
		Long questionId = answer.getQuestionId();  // answer 객체에 questionId가 있어야 함

		// 답변 삭제
		answerRepository.delete(answer);

		// 질문 상태 업데이트
		Optional<Question> questionOptional = questionRepository.findById(questionId);
		if (questionOptional.isPresent()) {
			Question question = questionOptional.get();

			// 해당 질문에 등록된 답변 개수 확인
			long answerCount = answerRepository.countByQuestionId(questionId);
			System.out.println("answerCount 개수 : " + answerCount);

			// 답변이 1개 이상 남아있으면 'ANSWERED' 상태로 유지, 0개면 'WAITING' 상태로 변경
			if (answerCount > 0) {
				question.setStatus(Question.QuestionStatus.ANSWERED);
			} else {
				question.setStatus(Question.QuestionStatus.WAITING);
			}

			// 변경된 상태 저장
			questionRepository.save(question);
		}
	}

	@Override
	public List<AnswerDTO> getAnswersByQuestion(Long questionId) {
		// 해당 질문에 대한 답변 목록을 가져오기
		List<Answer> answers = answerRepository.findByQuestionId(questionId);

		// 답변들을 DTO로 변환하여 반환
		return answers.stream()
			.map(answer -> {
				// AnswerDTO 생성 (질문 ID, 답변 내용, 작성자 ID, 작성일 등)
				return new AnswerDTO(
					answer.getAnswerId(),  // 답변 ID
					answer.getQuestionId(), // 질문 ID만 사용
					answer.getContent(),    // 답변 내용
					answer.getUserId(),     // 답변을 작성한 관리자 userId
					answer.getCreateDate(), // 답변 작성일
					answer.getStatus()
				);
			})
			.collect(Collectors.toList());
	}
}
