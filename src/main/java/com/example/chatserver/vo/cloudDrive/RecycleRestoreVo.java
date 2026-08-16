package com.example.chatserver.vo.cloudDrive;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;


@Data
public class RecycleRestoreVo {

    @NotEmpty(message = "回收站记录id列表不能为空~")
    private List<String> spaceRecycleIds;
}
