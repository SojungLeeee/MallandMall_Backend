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

	// 가까운 지점 재고 조회 로직
	@Override
	public List<BranchWithStockDTO> findNearestBranchesWithStock(double userLatitude, double userLongitude,
		List<String> productCodes, int limit) {
		// 모든 지점 정보 가져오기
		List<Branch> allBranches = branchRepository.findAll();

		// 지점별 거리 계산 및 정렬
		List<Branch> sortedBranches = allBranches.stream()
			.filter(branch -> branch.getLatitude() != null && branch.getLongitude() != null)
			.sorted((b1, b2) -> {
				double distance1 = calculateDistance(userLatitude, userLongitude,
					b1.getLatitude(), b1.getLongitude());
				double distance2 = calculateDistance(userLatitude, userLongitude,
					b2.getLatitude(), b2.getLongitude());
				return Double.compare(distance1, distance2);
			})
			.collect(Collectors.toList());

		// 결과 리스트 준비
		List<BranchWithStockDTO> result = new ArrayList<>();

		// 가까운 지점부터 재고 확인
		for (Branch branch : sortedBranches) {
			if (result.size() >= limit) {
				break; // 요청된 개수만큼 결과를 얻었으면 중단
			}

			double distance = calculateDistance(userLatitude, userLongitude,
				branch.getLatitude(), branch.getLongitude());

			// 해당 지점의 상품별 재고 확인
			Map<String, Integer> stockInfo = new HashMap<>();
			boolean hasAllStock = true;

			for (String productCode : productCodes) {
				// 인벤토리 서비스를 통해 재고 확인
				// 실제 구현에 맞게 수정 필요 - 현재는 가정: inventoryService가 지점별 상품 수량을 반환
				// 지점의 특정 상품 수량 조회 메소드 필요
				// 예: Integer quantity = inventoryService.getQuantityByProductAndBranch(productCode, branch.getBranchName());

				// 지점의 모든 인벤토리 조회 메소드 필요
				// 제공된 코드는 상품 코드별 지점 수량을 반환하는 메소드가 있지만, 반대 방향 조회 메소드 필요
				Map<String, Integer> productQuantities = inventoryService.findQuantityByProductCode(productCode);

				int quantity = productQuantities.getOrDefault(branch.getBranchName(), 0);
				stockInfo.put(productCode, quantity);

				// 하나라도 재고가 없으면 전체 재고 없음으로 표시
				if (quantity <= 0) {
					hasAllStock = false;
				}
			}

			// DTO 생성하여 결과에 추가
			BranchWithStockDTO branchDTO = BranchWithStockDTO.builder()
				.branchName(branch.getBranchName())
				.branchAddress(branch.getBranchAddress())
				.latitude(branch.getLatitude())
				.longitude(branch.getLongitude())
				.distance(distance)
				.hasStock(hasAllStock)
				.stockDetails(stockInfo)
				.build();

			result.add(branchDTO);
		}

		return result;
	}

	//커스텀 예외(지점 수정에서 사용 중)
	public class BranchNotFoundException extends RuntimeException {
		public BranchNotFoundException(String message) {
			super(message);
		}
	}
}

