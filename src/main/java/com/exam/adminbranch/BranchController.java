package com.exam.adminbranch;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/branch")
public class BranchController {

	private final BranchService branchService;

	public BranchController(BranchService branchService) {
		this.branchService = branchService;
	}

	//모든 지점 조회
	@GetMapping("/all")
	public ResponseEntity<?> getAllBranches() {
		try {
			List<Branch> branches = branchService.getAllBranches();
			List<BranchDTO> branchDTO = branches.stream()
				.map(branch -> BranchDTO.builder()
					.branchName(branch.getBranchName())
					.branchAddress(branch.getBranchAddress())
					.build())
				.collect(Collectors.toList());

			return ResponseEntity.ok(branchDTO);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("지점 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	//DTO 가져와서 쓰려면 RequestBody로 받아야한다.RequestBody는 객체 받아올때 사용한다.
	//지점 생성
	 @PostMapping("/create")
	 public ResponseEntity<?> createBranch(@RequestBody BranchDTO branchDTO){
		try {
			//1.빌더 패턴을 사용하여 객체 생성
			Branch branch = Branch.builder()
				.branchName(branchDTO.getBranchName())
				.branchAddress(branchDTO.getBranchAddress())
				.build();
			//2. 브랜치서비스의 비즈니스 로직 가져오기
			Branch createdBranch = branchService.createBranches(branch);

			//3. DTO를 통해서 클라이언트에게 반환
			BranchDTO responseDTO = BranchDTO.builder()
				.branchName(createdBranch.getBranchName())
				.branchAddress(createdBranch.getBranchAddress())
				.build();
			//4. 응답 엔티티를 통해서 상태코드 반환
			return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
		} catch(RuntimeException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(e.getMessage());
		} catch(Exception e){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("지점 생성 중 오류가 발생했습니다: " + e.getMessage());
		}
	 }

	 //지점 수정
	 @PutMapping("update/{branchName}")
	public ResponseEntity<?> updateBranch(@PathVariable String branchName, @RequestBody BranchDTO branchDTO){
		try{
			Branch branch = Branch.builder()
				.branchName(branchDTO.getBranchName())
				.branchAddress(branchDTO.getBranchAddress())
				.build();

			Branch updateBranch = branchService.updateBranches(branch);

			BranchDTO responseDTO = BranchDTO.builder()
				.branchName(updateBranch.getBranchName())
				.branchAddress(updateBranch.getBranchAddress())
				.build();
			return ResponseEntity.ok(responseDTO);
		} catch (BranchServiceImpl.BranchNotFoundException e){
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("지점 수정 중 오류가 발생했습니다: " + e.getMessage());
		}
	 }

	 //지점 삭제
	@DeleteMapping("delete/{branchName}")
	public ResponseEntity<?> deleteBranch(@PathVariable String branchName){
		try{
			branchService.deleteBranches(null, branchName);
			return ResponseEntity.ok("지점이 성공적으로 삭제되었습니다: " + branchName);
		} catch (BranchServiceImpl.BranchNotFoundException e){
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(e.getMessage());
		} catch (Exception e){
			return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("지점 삭제 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
}
