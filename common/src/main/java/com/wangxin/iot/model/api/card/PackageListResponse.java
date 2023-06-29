package com.wangxin.iot.model.api.card;

import lombok.Data;

@Data
public class PackageListResponse {

    private String packageName;

    private String validStart;

    private String validEnd;

    @Override
    public String toString() {
        return "PackageListResponse [packageName=" + packageName + ", validStart=" + validStart + ", validEnd= "+validEnd+"]";
    }
}
