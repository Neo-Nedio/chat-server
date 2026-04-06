package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.TalkContentDto;
import com.example.chatserver.dto.TalkListDto;
import com.example.chatserver.entity.Talk;
import com.example.chatserver.entity.TalkPermission;
import com.example.chatserver.mapper.TalkMapper;
import com.example.chatserver.service.TalkPermissionService;
import com.example.chatserver.service.TalkService;
import com.example.chatserver.vo.talk.CreateTalkVo;
import com.example.chatserver.vo.talk.TalkListVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class TalkServiceImpl extends ServiceImpl<TalkMapper, Talk> implements TalkService {

    @Resource
    TalkMapper talkMapper;

    @Resource
    TalkPermissionService talkPermissionService;

    @Override
    public List<TalkListDto> talkList(String userId, TalkListVo talkListVo) {
        return talkMapper.talkList(userId, talkListVo.getIndex(), talkListVo.getNum());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Talk createTalk(String userId, CreateTalkVo createTalkVo) {
        //新增说说
        Talk talk = new Talk();
        String talkId = IdUtil.randomUUID();
        talk.setId(talkId);
        talk.setUserId(userId);
        TalkContentDto talkContentDto = new TalkContentDto();
        talkContentDto.setText(createTalkVo.getText());
        talkContentDto.setImg(new ArrayList<>());
        talk.setContent(talkContentDto);
        save(talk);

        //新增说说查看权限
        List<String> permissions = createTalkVo.getPermission();
        if (null == permissions || permissions.isEmpty()) {
            //全部人可查看
            TalkPermission talkPermission = new TalkPermission();
            talkPermission.setId(IdUtil.randomUUID());
            talkPermission.setTalkId(talkId);
            talkPermission.setPermission("all");
            talkPermissionService.save(talkPermission);
        } else {
            //特定人可查看
            List<TalkPermission> talkPermissionList = new ArrayList<>();
            for (String permission : permissions) {
                TalkPermission talkPermission = new TalkPermission();
                talkPermission.setId(IdUtil.randomUUID());
                talkPermission.setTalkId(talkId);
                talkPermission.setPermission(permission);
                talkPermissionList.add(talkPermission);
            }
            talkPermissionService.saveBatch(talkPermissionList);
        }
        return talk;
    }

    @Override
    public Talk updateTalkImg(String userId, String talkId, String imgName) {
        LambdaQueryWrapper<Talk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Talk::getUserId, userId).eq(Talk::getId, talkId);
        Talk talk = getOne(queryWrapper);
        TalkContentDto content = talk.getContent();
        List<String> imgs = content.getImg();
        if (null == imgs) {
            imgs = new ArrayList<>();
        }
        imgs.add(imgName);
        updateById(talk);
        return talk;
    }
}
