//package com.smartgaon.ai.smartgaon_api.villageofficer.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import com.smartgaon.ai.smartgaon_api.villageofficer.model.GovernmentOffer;
//import com.smartgaon.ai.smartgaon_api.villageofficer.service.GovernmentOfferServiceImpl;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/offers")
//@RequiredArgsConstructor
//public class GovernmentOfferController {
//
//    private final GovernmentOfferServiceImpl offerService;
//
//    @PostMapping
//    public GovernmentOffer addOffer(@RequestBody GovernmentOffer offer) {
//        return offerService.addOffer(offer);
//    }
//
//
//    @GetMapping("/district/{district}")
//    public List<GovernmentOffer> getOffersByDistrict(
//            @PathVariable String district) {
//
//        return offerService.getOffersByDistrict(district.trim());
//    }
//
//    @GetMapping("/{id}")
//    public GovernmentOffer getOffer(@PathVariable Long id) {
//        return offerService.getOfferById(id);
//    }
//
//    @PutMapping("/{id}")
//    public GovernmentOffer updateOffer(@PathVariable Long id,
//                                       @RequestBody GovernmentOffer offer) {
//        return offerService.updateOffer(id, offer);
//    }
//
//    @DeleteMapping("/{id}")
//    public String deleteOffer(@PathVariable Long id) {
//        offerService.deleteOffer(id);
//        return "Deleted Successfully";
//    }
//}

package com.smartgaon.ai.smartgaon_api.villageofficer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.villageofficer.model.GovernmentOffer;
import com.smartgaon.ai.smartgaon_api.villageofficer.service.GovernmentOfferService;

import java.util.List;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
public class GovernmentOfferController {

    private final GovernmentOfferService offerService;

    @GetMapping("/district/{district}")
    public List<GovernmentOffer> getOffersByDistrict(
            @PathVariable String district) {

        return offerService.getOffersByDistrict(district.trim());
    }
}