package com.exam.adminbranch;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.exam.admin.AdminRepositoryGoods;
import com.exam.admin.Goods;
import com.exam.inventory.InventoryDTO;
import com.exam.inventory.InventoryService;
import com.exam.product.Product;
import com.exam.product.ProductRepository;

import jakarta.transaction.Transactional;

@Service // 비즈니스 로직 애노테이션
public class BranchServiceImpl implements BranchService {

	private final BranchRepository branchRepository; // 데이터 접근 로직을 위한 리포지토리
	private final AdminRepositoryGoods adminRepositoryGoods;
	private final ProductRepository productRepository;
	//DI를 통해서 인벤토리의 값 가져옴
	private final InventoryService inventoryService;

	public BranchServiceImpl(BranchRepository branchRepository, AdminRepositoryGoods adminRepositoryGoods,
		ProductRepository productRepository, InventoryService inventoryService) {
		this.branchRepository = branchRepository;
		this.adminRepositoryGoods = adminRepositoryGoods;
		this.productRepository = productRepository;
		this.inventoryService = inventoryService;
	}

	//1. 모든 지점 조회
	@Override
	public List<Branch> getAllBranches() {
		// 데이터베이스에서 모든 지점을 조회
		return branchRepository.findAll();
	}

	@Override
	public Branch getBranchByName(String branchName) {
		// findByBranchName 메소드를 사용하여 지점을 찾고, 없으면 예외 발생
		return branchRepository.findByBranchName(branchName)
			.orElseThrow(() -> new BranchNotFoundException("지점을 찾을 수 없습니다: " + branchName));
	}

	//2. 지점 생성
	@Override
	public Branch createBranches(Branch branch) {
		//Null 체크 Optional 로직
		Optional<Branch> existingBranch = branchRepository.findById(branch.getBranchName());
		if (existingBranch.isPresent()){
			throw new RuntimeException("지점 이름이 이미 존재합니다: " + branch.getBranchName());
		}
		return branchRepository.save(branch);
	}

	//3. 지점 수정
	@Override
	@Transactional //트랜젝션 자동 관리 애노테이션 로직이 종료되면 자동 Commit
	public Branch updateBranches(Branch branch) {
		Branch existingBranch = branchRepository.findById(branch.getBranchName())
			.orElseThrow(() -> new BranchNotFoundException("지점을 찾을 수 없습니다.: " + branch.getBranchName()));

		existingBranch.setBranchAddress(branch.getBranchAddress());
		existingBranch.setBranchName(branch.getBranchName());
		// 위도, 경도 값도 업데이트
		if (branch.getLatitude() != null) {
			existingBranch.setLatitude(branch.getLatitude());
		}
		if (branch.getLongitude() != null) {
			existingBranch.setLongitude(branch.getLongitude());
		}
		return branchRepository.save(existingBranch);
	}

	//4. 지점 삭제
	@Override
	@Transactional
	public void deleteBranches(Branch branch, String branchName) {
		Branch existingBranch = branchRepository.findById(branchName)
			.orElseThrow(() -> new BranchNotFoundException("지점을 찾을 수 없습니다: " + branchName));

		branchRepository.delete(existingBranch);
	}

	//지도 API용 메소드
	// 특정 상품을 보유한 지점 목록 조회
	@Override
	public List<BranchLocationDTO> getBranchesWithProduct(String productCode) {
		// 모든 지점 정보 가져오기
		List<Branch> branches = branchRepository.findAll();
		List<BranchLocationDTO> result = new ArrayList<>();

		// 해당 상품의 지점별 수량 조회
		Map<String, Integer> productQuantities = inventoryService.findQuantityByProductCode(productCode);

		for (Branch branch : branches) {
			// 해당 상품이 있는 지점만 포함
			if (productQuantities.containsKey(branch.getBranchName()) &&
				productQuantities.get(branch.getBranchName()) > 0) {

				BranchLocationDTO dto = new BranchLocationDTO();
				dto.setBranchName(branch.getBranchName());
				dto.setBranchAddress(branch.getBranchAddress());
				dto.setLatitude(branch.getLatitude());
				dto.setLongitude(branch.getLongitude());

				// 해당 상품의 수량 설정
				dto.setGoodsCount(productQuantities.get(branch.getBranchName()));

				result.add(dto);
			}
		}

		return result;
	}

	// 지점명 또는 주소로 검색
	@Override
	public List<BranchLocationDTO> searchBranches(String keyword) {
		List<Branch> branches = branchRepository.findAll();
		List<BranchLocationDTO> result = new ArrayList<>();

		// 모든 지점 정보를 DTO로 변환
		for (Branch branch : branches) {
			BranchLocationDTO dto = new BranchLocationDTO();
			dto.setBranchName(branch.getBranchName());
			dto.setBranchAddress(branch.getBranchAddress());
			dto.setLatitude(branch.getLatitude());
			dto.setLongitude(branch.getLongitude());
			result.add(dto);
		}

		if (keyword == null || keyword.isEmpty()) {
			return result;
		}

		String lowerKeyword = keyword.toLowerCase();

		return result.stream()
			.filter(branch ->
				branch.getBranchName().toLowerCase().contains(lowerKeyword) ||
					(branch.getBranchAddress() != null &&
						branch.getBranchAddress().toLowerCase().contains(lowerKeyword))
			)
			.collect(Collectors.toList());
	}

	@Override
	public BranchDTO findNearestBranch(Double latitude, Double longitude) {
		if(latitude == null || longitude == null){
			throw new IllegalArgumentException("위도와 경도가 필요합니다");
		}

		List<Branch> branches = branchRepository.findAll();
		Branch nearestBranch = null;
		double minDistance = Double.MAX_VALUE;

		for (Branch branch : branches) {
			if (branch.getLatitude() != null && branch.getLongitude() != null){
				double distance = calculateDistance(
					latitude, longitude,
					branch.getLatitude(), branch.getLongitude()
				);
				if (distance < minDistance){
					minDistance = distance;
					nearestBranch = branch;
				}
			}
		}
		if (nearestBranch == null){
			throw new RuntimeException("유효한 위치 정보가 있는 지점을 찾을 수 없습니다");
		}

		//DTO 로 변환하고 거리 정보 추가
		BranchDTO branchDTO = BranchDTO.builder()
			.branchName(nearestBranch.getBranchName())
			.branchAddress(nearestBranch.getBranchAddress())
			.latitude(nearestBranch.getLatitude())
			.longitude(nearestBranch.getLongitude())
			.distance(minDistance)
			.build();

		return  branchDTO;
	}

	// 두 지점 간의 거리 계산 메소드 (Haversine 공식)
	private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		final int R = 6371; // 지구의 반지름 (km)
		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
			+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
			* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}

	//커스텀 예외(지점 수정에서 사용 중)
	public class BranchNotFoundException extends RuntimeException {
		public BranchNotFoundException(String message) {
			super(message);
		}
	}
}

