package com.smartgaon.ai.smartgaon_api.scheme.dto;

import com.smartgaon.ai.smartgaon_api.scheme.entity.Language;
import com.smartgaon.ai.smartgaon_api.scheme.entity.Scheme;
import com.smartgaon.ai.smartgaon_api.scheme.entity.SchemeContent;
import com.smartgaon.ai.smartgaon_api.scheme.entity.SchemeType;
import com.smartgaon.ai.smartgaon_api.scheme.entity.State;

public record UserSchemeResponse(
        Long id,
        SchemeType schemeType,
        State state,
        String title,
        String detail,
        String benefits,
        String eligibility,
        UserCategoryResponse category,
        String imageUrl,
        String schemeUrl,
        Long interestCount,
        String language,     // language requested, e.g. "HI"
        boolean translated   // false = no translation in this language, English shown
) {

    public static UserSchemeResponse of(Scheme s, Language lang) {
        SchemeContent tr = (lang == Language.DEFAULT) ? null : s.getTranslations().get(lang);
        boolean translated = lang == Language.DEFAULT || (tr != null && !tr.isEmpty());

        return new UserSchemeResponse(
                s.getId(),
                s.getSchemeType(),
                s.getState(),
                pick(tr == null ? null : tr.getTitle(), s.getTitle()),
                pick(tr == null ? null : tr.getDetail(), s.getDetail()),
                pick(tr == null ? null : tr.getBenefits(), s.getBenefits()),
                pick(tr == null ? null : tr.getEligibility(), s.getEligibility()),
                UserCategoryResponse.of(s.getCategory(), lang),
                s.getImageUrl(),
                s.getSchemeUrl(),
                s.getInterestCount() == null ? 0L : s.getInterestCount(),
                lang.name(),
                translated
        );
    }

    private static String pick(String translated, String fallback) {
        return (translated == null || translated.isBlank()) ? fallback : translated;
    }
}
