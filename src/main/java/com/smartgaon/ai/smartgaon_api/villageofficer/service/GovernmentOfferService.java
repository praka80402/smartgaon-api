package com.smartgaon.ai.smartgaon_api.villageofficer.service;

import com.smartgaon.ai.smartgaon_api.villageofficer.model.GovernmentOffer;

import java.util.List;

public interface GovernmentOfferService {

    List<GovernmentOffer> getOffersByDistrict(String district);

}
