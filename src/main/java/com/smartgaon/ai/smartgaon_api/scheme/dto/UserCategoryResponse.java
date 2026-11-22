package com.smartgaon.ai.smartgaon_api.scheme.dto;

import com.smartgaon.ai.smartgaon_api.scheme.entity.Category;
import com.smartgaon.ai.smartgaon_api.scheme.entity.Language;

/** Category for the website, name already in the requested language. */
public record UserCategoryResponse(Long id, String name) {

    public static UserCategoryResponse of(Category c, Language lang) {
        if (c == null) {
            return null;
        }
        return new UserCategoryResponse(c.getId(), c.localizedName(lang));
    }
}
