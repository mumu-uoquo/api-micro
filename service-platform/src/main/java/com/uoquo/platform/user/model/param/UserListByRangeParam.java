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

    @Schema(description = "游标：上一批最后一个用户ID（空字符串表示从头开始）。不为空时使用游标分页（按用户ID排序，忽略pageNum，每批pageSize条，不查询总数），适合群发等需要完整遍历的场景")
    private String afterId;

    public String getAfterId() {
        return afterId;
    }

    public void setAfterId(String afterId) {
        this.afterId = afterId;
    }

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
