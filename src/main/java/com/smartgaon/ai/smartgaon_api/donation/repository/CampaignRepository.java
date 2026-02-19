//package com.smartgaon.ai.smartgaon_api.donation.repository;

//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//
//import com.smartgaon.ai.smartgaon_api.donation.enums.CampaignType;
//import com.smartgaon.ai.smartgaon_api.donation.model.DonationCampaign;
//
//public interface CampaignRepository extends JpaRepository<DonationCampaign, Long> {
//
//    List<DonationCampaign> findByType(CampaignType type);
//    
//
//}

package com.smartgaon.ai.smartgaon_api.donation.repository;

import com.smartgaon.ai.smartgaon_api.donation.enums.CampaignStatus;
import com.smartgaon.ai.smartgaon_api.donation.enums.CampaignType;
import com.smartgaon.ai.smartgaon_api.donation.model.DonationCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<DonationCampaign, Long> {
	
	   List<DonationCampaign> findByTypeAndStatus(CampaignType type, CampaignStatus status);

//    // PROGRAM → visible to all users
//    List<DonationCampaign> findByTypeAndStatus(CampaignType type, CampaignStatus status);
//
//    // PROJECT → filter by state
//    List<DonationCampaign> findByTypeAndStatusAndStateOrTypeAndStatusAndState(
//            CampaignType type1, String status1, String state1,
//            CampaignType type2, String status2, String state2
//    );
}
