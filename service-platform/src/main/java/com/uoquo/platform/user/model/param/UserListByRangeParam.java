package com.uoquo.platform.user.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 入参：根据范围查询用户列表
 */
@Schema(description = "根据范围查询用户列表")
public class UserListByRangeParam extends PageRequest {

    @Schema(description = "发布范围（024）")
    private String receiverRange;

    @Schema(description = "目标所属机构")
    private String receiverInstituteId;

    @Schema(description = "目标ID集合")
    private List<String> receiverIds;

    public String getReceiverRange() {
        return receiverRange;
    }

    public void setReceiverRange(String receiverRange) {
        this.receiverRange = receiverRange;
    }

    public String getReceiverInstituteId() {
        return receiverInstituteId;
    }

    public void setReceiverInstituteId(String receiverInstituteId) {
        this.receiverInstituteId = receiverInstituteId;
    }

    public List<String> getReceiverIds() {
        return receiverIds;
    }

    public void setReceiverIds(List<String> receiverIds) {
        this.receiverIds = receiverIds;
    }


}
