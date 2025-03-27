package com.exam.adminbranch;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.exam.admin.AdminRepositoryGoods;
import com.exam.admin.Goods;
import com.exam.product.Product;
import com.exam.product.ProductRepository;

import jakarta.transaction.Transactional;

@Service // 비즈니스 로직 애노테이션
public class BranchServiceImpl implements BranchService {

	private final BranchRepository branchRepository; // 데이터 접근 로직을 위한 리포지토리
	private final AdminRepositoryGoods adminRepositoryGoods;
	private final ProductRepository productRepository;

	// 생성자를 통한 의존성 주입
	public BranchServiceImpl(BranchRepository branchRepository, AdminRepositoryGoods adminRepositoryGoods,
		ProductRepository productRepository) {
		this.branchRepository = branchRepository;
		this.adminRepositoryGoods = adminRepositoryGoods;
		this.productRepository = productRepository;
	}


	//모든 지점 조회
	@Override
	public List<Branch> getAllBranches() {
		// 데이터베이스에서 모든 지점을 조회
		return branchRepository.findAll();
	}

	//지점 생성
	@Override
	public Branch createBranches(Branch branch) {
		//Null 체크 Optional 로직
		Optional<Branch> existingBranch = branchRepository.findById(branch.getBranchName());
		if (existingBranch.isPresent()){
			throw new RuntimeException("지점 이름이 이미 존재합니다: " + branch.getBranchName());
		}
		return branchRepository.save(branch);
	}

	//지점 수정
	@Override
	@Transactional //트랜젝션 자동 관리 애노테이션 로직이 종료되면 자동 Commit
	public Branch updateBranches(Branch branch) {
		Branch existingBranch = branchRepository.findById(branch.getBranchName())
			.orElseThrow(() -> new BranchNotFoundException("지점을 찾을 수 없습니다.: " + branch.getBranchName()));

		existingBranch.setBranchAddress(branch.getBranchAddress());
		existingBranch.setBranchName(branch.getBranchName());
		return branchRepository.save(existingBranch);

	}

	//지점 삭제
	@Override
	@Transactional
	public void deleteBranches(Branch branch, String branchName) {
		Branch existingBranch = branchRepository.findById(branchName)
			.orElseThrow(() -> new BranchNotFoundException("지점을 찾을 수 없습니다: " + branchName));

		branchRepository.delete(existingBranch);
	}

	// 지도 API용 메소드 구현 (AdminRepositoryGoods 사용)
	@Override
	public List<BranchLocationDTO> getAllBranchesWithGoodsCount() {
		List<Branch> branches = branchRepository.findAll();
		List<BranchLocationDTO> result = new ArrayList<>();

		// 모든 상품 조회
		List<Goods> allGoods = adminRepositoryGoods.findAll();

		// 지점별로 상품 그룹화
		Map<String, List<Goods>> goodsByBranch = allGoods.stream()
			.collect(Collectors.groupingBy(Goods::getBranchName));

		for (Branch branch : branches) {
			BranchLocationDTO dto = new BranchLocationDTO();
			dto.setBranchName(branch.getBranchName());
			dto.setBranchAddress(branch.getBranchAddress());
			dto.setLatitude(branch.getLatitude());
			dto.setLongitude(branch.getLongitude());

			// 해당 지점의 상품 수 계산
			List<Goods> branchGoods = goodsByBranch.getOrDefault(branch.getBranchName(), new ArrayList<>());
			dto.setGoodsCount(branchGoods.size());

			result.add(dto);
		}

		return result;
	}

	@Override
	public BranchLocationDTO getBranchDetailWithGoods(String branchName) {
		Branch branch = branchRepository.findById(branchName)
			.orElseThrow(() -> new BranchNotFoundException("지점을 찾을 수 없습니다: " + branchName));

		BranchLocationDTO dto = new BranchLocationDTO();
		dto.setBranchName(branch.getBranchName());
		dto.setBranchAddress(branch.getBranchAddress());
		dto.setLatitude(branch.getLatitude());
		dto.setLongitude(branch.getLongitude());

		// AdminRepositoryGoods를 사용하여 해당 지점의 모든 상품 조회
		List<Goods> allGoods = adminRepositoryGoods.findAll();
		List<Goods> branchGoods = allGoods.stream()
			.filter(g -> g.getBranchName().equals(branchName))
			.collect(Collectors.toList());

		dto.setGoodsCount(branchGoods.size());

		// 상품 코드별로 그룹핑하여 개수 카운트
		Map<String, Long> productCountMap = branchGoods.stream()
			.collect(Collectors.groupingBy(Goods::getProductCode, Collectors.counting()));

		// 상품 상세 정보 설정
		List<GoodsInfoDTO> goodsInfos = new ArrayList<>();

		for (Map.Entry<String, Long> entry : productCountMap.entrySet()) {
			String productCode = entry.getKey();
			long count = entry.getValue();

			// 상품 정보 조회
			Product product = productRepository.findByProductCode(productCode);

			if (product != null) {
				GoodsInfoDTO goodsInfo = new GoodsInfoDTO();
				goodsInfo.setProductCode(productCode);
				goodsInfo.setProductName(product.getProductName());
				goodsInfo.setCount(Math.toIntExact(count));

				goodsInfos.add(goodsInfo);
			}
		}

		dto.setGoods(goodsInfos);

		return dto;
	}

	//커스텀 예외(지점 수정에서 사용 중)
	public class BranchNotFoundException extends RuntimeException {
		public BranchNotFoundException(String message) {
			super(message);
		}
	}
}