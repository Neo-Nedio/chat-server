# Chat Server API 文档

> 本文档根据当前项目 Controller、VO 和 Service 实现整理，适用于 `chat-server` 当前版本。接口行为变化时，请以代码为准。

## 1. 服务地址

| 服务 | 默认地址 | 说明 |
| --- | --- | --- |
| HTTP API | `http://{host}:9200` | Spring Boot REST API |
| WebSocket | `ws://{host}:9100/ws?x-token={token}` | Netty 实时消息、通知和通话信令 |

HTTP 接口的用户端前缀为 `/v1/api`，管理端前缀为 `/admin/v1/api`。

### 请求约定

- JSON 请求使用 `Content-Type: application/json`。
- 文件表单使用 `multipart/form-data`，文件字段一般为 `file`。
- 原始文件上传接口直接读取 HTTP body，文件元数据通过请求头传递。
- 服务端时间格式为 `yyyy-MM-dd HH:mm:ss.SSS`，时区为 `Asia/Shanghai`。
- 除特别说明外，所有接口都需要在请求头携带 `x-token`。

## 2. 认证与统一响应

### 2.1 JWT 认证

登录成功后，从响应 `data.token` 获取 JWT，并在后续请求中携带：

```http
x-token: {token}
```

JWT 默认有效期为 30 天。登录、注册、邮箱验证码、找回密码、获取 RSA 公钥，以及标记为公开的二维码接口不强制要求 Token；公开接口如果携带有效 Token，服务端仍会解析并注入用户信息。用户搜索、二维码扫码确认等未标记为公开的接口仍需要 Token。

密码相关接口的 `password` 字段要求使用 `/v1/api/login/public-key` 返回的 RSA 公钥加密，再进行 Base64 编码。涉及密码的接口包括登录、注册和找回密码；修改密码接口的 `confirmPassword` 也按当前实现要求加密。

角色值：

| 值 | 含义 |
| --- | --- |
| `user` | 普通用户 |
| `admin` | 管理员 |
| `third` | 第三方会话用户 |

### 2.2 统一 JSON 响应

成功且无数据：

```json
{
  "code": 0,
  "msg": "操作成功"
}
```

成功且有数据：

```json
{
  "code": 0,
  "data": {}
}
```

失败响应：

```json
{
  "code": 1,
  "msg": "操作失败"
}
```

| `code` | 含义 |
| ---: | --- |
| `0` | 成功 |
| `1` | 业务失败 |
| `-1` | Token 无效或账号已被禁用 |
| `-2` | 当前角色无权限 |

认证过滤器拦截时通常仍返回 HTTP `200`，请以前述 JSON 的 `code` 判断业务结果。

## 3. 用户端公开与账号接口

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/v1/api/login/public-key` | 无 | RSA 公钥字符串 | 获取密码加密公钥 |
| `POST` | `/v1/api/login` | `LoginVo` | 登录用户信息和 `token` | 密码需 RSA 加密 |
| `POST` | `/v1/api/user/register` | `RegisterVo` | 无 | 密码和验证码校验 |
| `POST` | `/v1/api/user/email/verify` | `EmailVerifyVo` | 无 | 向邮箱发送验证码 |
| `POST` | `/v1/api/user/email/verify/by/account` | `EmailVerifyByAccountVo` | 无 | 根据账号对应邮箱发送验证码 |
| `POST` | `/v1/api/user/forget` | `ForgetVo` | 无 | 找回密码，密码需 RSA 加密 |
| `POST` | `/v1/api/login/qr` | `QrCodeLoginVo` | 登录用户信息和 `token` | 已登录移动端确认 PC 扫码登录 |
| `POST` | `/v1/api/login/logout` | Header `x-token` | 无 | 退出当前 Token |
| `POST` | `/v1/api/user/search` | `SearchUserVo` | 用户列表 | 用户搜索 |

`LoginVo` 示例：

```json
{
  "account": "demo",
  "password": "{RSA_BASE64_PASSWORD}",
  "onlineEquipment": "web",
  "pushyToken": ""
}
```

登录成功的 `data` 包含 `userId`、`account`、`username`、`role`、`portrait`、`sex`、`phone`、`email`、`status` 和 `token`。

## 4. 用户信息与设置

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/v1/api/user/info` | 无 | `UserDto` | 当前用户信息 |
| `GET` | `/v1/api/user/info/id?toId={userId}` | Query `toId` | 用户实体 | 查询指定用户 |
| `GET` | `/v1/api/user/unread` | 无 | 未读数量 Map | 获取各项未读数 |
| `POST` | `/v1/api/user/update` | `UpdateVo` | 无 | 修改昵称、性别、生日、签名、头像等 |
| `POST` | `/v1/api/user/update/password` | `UpdatePasswordVo` | 无 | 修改密码，需校验旧密码 |
| `POST` | `/v1/api/user/upload/portrait` | 原始文件；Headers `name`、`type`、`size` | 文件名 | 上传头像 |
| `POST` | `/v1/api/user/upload/portrait/form` | Form `file`、`name`、`type`、`size` | 文件名 | 表单方式上传头像 |
| `GET` | `/v1/api/user/get/portrait?fileName={fileName}` | Query `fileName` | 临时 URL | 获取头像访问地址 |
| `GET` | `/v1/api/user/get/file` | Headers `targetId`、`fileName` | 文件流 | 获取聊天文件 |
| `GET` | `/v1/api/user/get/img?targetId={id}&fileName={name}` | Query `targetId`、`fileName` | 临时 URL | 获取聊天图片 |
| `POST` | `/v1/api/user/set-chat-background` | Form `file`、`name`、`type`、`size` | 临时 URL | 设置聊天背景 |
| `GET` | `/v1/api/user/get-chat-background` | 无 | 临时 URL | 获取聊天背景 |
| `GET` | `/v1/api/user-set` | 无 | `UserSet` | 获取用户设置 |
| `POST` | `/v1/api/user-set/update` | `UpdateUserSetVo` | 无 | 更新指定设置项 |

## 5. 好友、好友分组与通知

### 5.1 好友

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/v1/api/friend/list` | 无 | 分组好友列表 | 获取好友列表 |
| `GET` | `/v1/api/friend/list/flat?friendInfo={keyword}` | Query `friendInfo`，可空 | 平铺好友列表 | 获取平铺好友列表 |
| `GET` | `/v1/api/friend/list/flat/unread?friendInfo={keyword}` | Query `friendInfo`，可空 | 平铺好友列表 | 附带未读数 |
| `GET` | `/v1/api/friend/is/friend?targetId={userId}` | Query `targetId` | Boolean | 判断是否为好友 |
| `GET` | `/v1/api/friend/details/{friendId}` | Path `friendId` | `FriendDetailsDto` | 好友详情 |
| `POST` | `/v1/api/friend/search` | `SearchVo` | 好友列表 | 搜索好友 |
| `POST` | `/v1/api/friend/agree` | `AgreeFriendApplyVo` | Boolean | 同意指定好友申请 |
| `POST` | `/v1/api/friend/agree/id` | `AgreeFriendApplyVo` | Boolean | 按发起人批量同意 |
| `POST` | `/v1/api/friend/reject` | `RejectFriendApplyVo` | Boolean | 拒绝好友申请 |
| `POST` | `/v1/api/friend/add/qr` | `AddFriendByQrVo` | Boolean | 旧二维码加好友接口，已废弃 |
| `POST` | `/v1/api/friend/set/remark` | `SetRemarkVo` | Boolean | 设置好友备注 |
| `POST` | `/v1/api/friend/set/group` | `SetGroupVo` | Boolean | 设置好友分组 |
| `POST` | `/v1/api/friend/delete` | `DeleteFriendVo` | 无 | 删除好友 |
| `POST` | `/v1/api/friend/carefor` | `CareForFriendVo` | 无 | 设置特别关心 |
| `POST` | `/v1/api/friend/uncarefor` | `UnCareForFriendVo` | 无 | 取消特别关心 |

### 5.2 好友分组

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `POST` | `/v1/api/group/create` | `CreateGroupVo` | 无 |
| `POST` | `/v1/api/group/update` | `UpdateGroupVo` | 无 |
| `POST` | `/v1/api/group/delete` | `DeleteGroupVo` | 无 |
| `GET` | `/v1/api/group/list` | 无 | 分组列表 |

### 5.3 通知

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/v1/api/notify/list` | 无 | 好友/入群申请列表 | 按时间倒序 |
| `POST` | `/v1/api/notify/friend/apply` | `FriendApplyNotifyVo` | Boolean | 发起好友申请 |
| `POST` | `/v1/api/notify/group/apply` | `GroupApplyNotifyVo` | Boolean | 发起入群申请 |
| `POST` | `/v1/api/notify/read` | `ReadNotifyVo` | Boolean | 通知已读，类型见 `friend/group/system` |
| `GET` | `/v1/api/notify/group/read?groupId={groupId}` | Query `groupId` | 无 | 群通知已读 |
| `GET` | `/v1/api/notify/system/list` | 无 | 系统通知列表 |  |
| `GET` | `/v1/api/notify/system/latest` | 无 | 最新系统通知 |  |
| `GET` | `/v1/api/notify/system/read` | 无 | 无 | 系统通知已读 |
| `GET` | `/v1/api/notify/get/img?fileName={fileName}` | Query `fileName` | 临时 URL | 获取通知图片；当前实现不要求登录 |

## 6. 聊天会话、消息与群组

### 6.1 会话

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `GET` | `/v1/api/chat-list/list` | 无 | `ChatListDto`，含 `tops`、`others` |
| `POST` | `/v1/api/chat-list/search` | `SearchVo` | `ChatDto`，含好友和群组 |
| `POST` | `/v1/api/chat-list/create` | `CreateChatListVo` | `ChatList` |
| `POST` | `/v1/api/chat-list/delete` | `DeleteChatListVo` | 无 |
| `POST` | `/v1/api/chat-list/top` | `TopChatListVo` | 无 |
| `GET` | `/v1/api/chat-list/read/{targetId}` | Path `targetId` | 无 | 将目标会话标记为已读 |
| `GET` | `/v1/api/chat-list/read/all` | 无 | 无 | 全部已读 |
| `POST` | `/v1/api/chat-list/detail` | `DetailChatListVo` | `ChatList` |

`CreateChatListVo.type` 默认值为 `user`；群会话通常使用 `group`。

### 6.2 消息

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `POST` | `/v1/api/message/send` | `SendMsgVo` | `Message` | 发送文本、图片、文件、语音等消息 |
| `POST` | `/v1/api/message/retraction` | `RetractionMsgVo` | `Message` | 撤回消息 |
| `POST` | `/v1/api/message/reedit` | `ReeditMsgVo` | `MessageRetraction` | 重新编辑被撤回消息 |
| `POST` | `/v1/api/message/record` | `MessageRecordVo` | 消息列表 | 正序记录 |
| `POST` | `/v1/api/message/record/desc` | `MessageRecordVo` | 消息列表 | 倒序记录 |
| `POST` | `/v1/api/message/send/file` | 原始文件；Header `msgId` | 临时 URL | 原始 body 上传文件 |
| `POST` | `/v1/api/message/send/file/form` | Form `file`、`msgId` | 临时 URL | 表单上传文件 |
| `POST` | `/v1/api/message/send/Img` | 原始图片；Header `msgId` | 临时 URL | 路径中的 `Img` 大写敏感 |
| `GET` | `/v1/api/message/get/file` | Header `msgId` | 文件流 | 下载聊天文件 |
| `GET` | `/v1/api/message/get/media?msgId={msgId}` | Query `msgId` | 临时 URL | 获取图片/音频等媒体 URL |
| `GET` | `/v1/api/message/voice/to/text?msgId={id}&isChatGroupMessage={bool}` | Query `msgId`、`isChatGroupMessage` | `Message` | 语音转文字 |

`SendMsgVo.msgContent.type` 常用值：`text`、`img`、`file`、`voice`、`emoji`、`call`、`system`。消息来源 `source` 使用 `user` 或 `group`。

消息内容示例：

```json
{
  "toUserId": "target-user-id",
  "source": "user",
  "msgContent": {
    "type": "text",
    "content": "你好"
  },
  "isForward": false
}
```

### 6.3 群组

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/v1/api/chat-group/search?search={keyword}` | Query `search` | 群列表 | 搜索聊天群 |
| `GET` | `/v1/api/chat-group/list` | 无 | 群列表 | 当前用户所在群 |
| `POST` | `/v1/api/chat-group/create` | `CreateChatGroupVo` | 无 | 创建聊天群 |
| `POST` | `/v1/api/chat-group/update` | `UpdateChatGroupVo` | 无 | 更新个人群信息 |
| `POST` | `/v1/api/chat-group/update/name` | `UpdateChatGroupNameVo` | 无 | 更新群名称 |
| `POST` | `/v1/api/chat-group/invite` | `InviteMemberVo` | 无 | 邀请成员 |
| `POST` | `/v1/api/chat-group/quit` | `QuitChatGroupVo` | 无 | 退出群聊 |
| `POST` | `/v1/api/chat-group/kick` | `KickChatGroupVo` | 无 | 踢出成员 |
| `POST` | `/v1/api/chat-group/dissolve` | `DissolveChatGroupVo` | 无 | 解散群聊 |
| `POST` | `/v1/api/chat-group/isDissolve` | `DissolveChatGroupVo` | Boolean | 查询群是否已解散 |
| `POST` | `/v1/api/chat-group/transfer` | `TransferChatGroupVo` | 无 | 转让群主 |
| `POST` | `/v1/api/chat-group/agree` | `AgreeGroupApplyVo` | Boolean | 同意入群申请 |
| `POST` | `/v1/api/chat-group/reject` | `RejectGroupApplyVo` | Boolean | 拒绝入群申请 |
| `POST` | `/v1/api/chat-group/details` | `DetailsChatGroupVo` | `ChatGroupDetailsDto` | 群详情 |
| `POST` | `/v1/api/chat-group/upload/portrait` | 原始图片；Headers `groupId`、`name`、`type`、`size` | 文件名 | 群主上传群头像 |
| `POST` | `/v1/api/chat-group/upload/portrait/form` | Form `file`、`groupId`、`name`、`type`、`size` | 文件名 | 表单上传群头像 |

群成员：

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `POST` | `/v1/api/chat-group-member/list` | `MemberListVo` | 成员 Map |
| `POST` | `/v1/api/chat-group-member/list/page` | `MemberListVo` | 成员列表 |
| `GET` | `/v1/api/chat-group-member/isMember?groupId={groupId}` | Query `groupId` | 无 |
| `POST` | `/v1/api/chat-group-member/ban` | `BanMemberVo` | 无 |
| `POST` | `/v1/api/chat-group-member/is/ban` | `BanMemberVo` | 无 |

群公告：

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `POST` | `/v1/api/chat-group-notice/create` | `CreateNoticeVo` | 无 |
| `POST` | `/v1/api/chat-group-notice/list` | `NoticeListVo` | 公告列表 |
| `POST` | `/v1/api/chat-group-notice/delete` | `DeleteNoticeVo` | 无 |
| `POST` | `/v1/api/chat-group-notice/update` | `UpdateNoticeVo` | 无 |

## 7. 云盘与表情

### 7.1 云盘

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `POST` | `/v1/api/cloudDrive/space/info` | 无 | 空间信息 | 不存在时自动创建 |
| `POST` | `/v1/api/cloudDrive/file/list` | 无 | 文件列表 | 按更新时间倒序 |
| `POST` | `/v1/api/cloudDrive/file/delete` | `FileDeleteVo` | 无 | 移入回收站，默认保留 30 天 |
| `POST` | `/v1/api/cloudDrive/file/categoryStats` | 无 | 分类统计列表 | 数量和字节大小 |
| `GET` | `/v1/api/cloudDrive/file/download?spaceFileId={id}` | Query `spaceFileId` | 临时 URL | URL 默认 7 天有效 |
| `POST` | `/v1/api/cloudDrive/recycle/list` | 无 | 回收站列表 | 不展示过期记录 |
| `POST` | `/v1/api/cloudDrive/recycle/restore` | `RecycleRestoreVo` | 无 | 还原前校验容量 |
| `POST` | `/v1/api/cloudDrive/recycle/delete` | `RecycleDeleteVo` | 无 | 彻底删除，不可恢复 |
| `POST` | `/v1/api/cloudDrive/recycle/clear` | 无 | 无 | 清空回收站 |
| `POST` | `/v1/api/cloudDrive/upload/check` | `UploadCheckVo` | `CheckUploadResultDto` | 秒传和断点续传检查 |
| `POST` | `/v1/api/cloudDrive/upload/chunk` | Form `file`、`fileHash`、`chunkIndex` | 无 | 上传单个分片 |
| `POST` | `/v1/api/cloudDrive/upload/merge` | `UploadCheckVo` | 合并结果 | 合并全部分片 |

上传流程：先调用 `/upload/check`；若 `uploaded=true` 可直接结束，否则根据 `uploadedChunks` 跳过已存在分片，逐个调用 `/upload/chunk`，最后调用 `/upload/merge`。

### 7.2 表情

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `GET` | `/v1/api/emoji/list` | 无 | 表情列表 |
| `POST` | `/v1/api/emoji/add?emoji={emoji}` | Query `emoji` | 无 |
| `POST` | `/v1/api/emoji/delete?emoji={emoji}` | Query `emoji` | 无 |
| `POST` | `/v1/api/emoji/upload` | Form `file`、`name`、`type`、`size` | 文件名 |
| `GET` | `/v1/api/emoji/get?fileName={fileName}` | Query `fileName` | 临时 URL |

## 8. 动态、评论与点赞

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `POST` | `/v1/api/talk/list` | `TalkListVo` | 动态列表 | 支持分页和目标用户筛选 |
| `POST` | `/v1/api/talk/details` | `DetailsTalkVo` | 动态详情 |  |
| `POST` | `/v1/api/talk/create` | `CreateTalkVo` | `Talk` | 发布动态 |
| `POST` | `/v1/api/talk/upload/img` | 原始图片；Headers `talkId`、`name`、`type`、`size` | `Talk` | 动态图片上传 |
| `POST` | `/v1/api/talk/upload/img/form` | Form `file`、`talkId`、`name`、`size` | `Talk` | 表单图片上传 |
| `POST` | `/v1/api/talk/delete` | `DeleteTalkVo` | 无 | 删除动态 |
| `POST` | `/v1/api/talk-comment/create` | `CreateTalkCommentVo` | 无 | 创建评论 |
| `POST` | `/v1/api/talk-comment/list` | `TalkCommentListVo` | 评论列表 |  |
| `POST` | `/v1/api/talk-comment/delete` | `DeleteTalkCommentVo` | 无 | 删除评论 |
| `POST` | `/v1/api/talk-like/create` | `CreateTalkLikeVo` | 无 | 点赞 |
| `POST` | `/v1/api/talk-like/list` | `TalkLikeListVo` | 点赞用户列表 |  |
| `POST` | `/v1/api/talk-like/delete` | `DeleteTalkLikeVo` | 无 | 取消点赞 |

## 9. 直播、音视频与 AI

### 9.1 直播间

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `GET` | `/v1/api/live-room/info` | 无 | `LiveRoomInfoDto` |
| `POST` | `/v1/api/live-room/title` | `UpdateLiveRoomTitleVo` | 无 |
| `POST` | `/v1/api/live-room/upload/background` | 原始图片；Headers `name`、`type`、`size` | 文件名/URL |
| `GET` | `/v1/api/live-room/get/background?fileName={fileName}` | Query `fileName` | 临时 URL |
| `GET` | `/v1/api/live-room/danmaku/list?sessionId={sessionId}` | Query `sessionId` | 弹幕列表 |
| `POST` | `/v1/api/live-room/danmaku/send` | `PublishDanmakuVo` | `LiveDanmakuDto` | 内容最多 200 字符 |

### 9.2 单人音视频通话信令

以下接口通过 WebSocket 向目标用户转发信令，HTTP 响应仅表示服务端是否成功处理。

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `POST` | `/v1/api/video/invite` | `InviteVo` | 无 |
| `POST` | `/v1/api/video/accept` | `AcceptVo` | 无 |
| `POST` | `/v1/api/video/offer` | `OfferVo` | 无 |
| `POST` | `/v1/api/video/answer` | `AnswerVo` | 无 |
| `POST` | `/v1/api/video/candidate` | `CandidateVo` | 无 |
| `POST` | `/v1/api/video/hangup` | `HangupVo` | 无 |

`OfferVo.desc`、`AnswerVo.desc`、`CandidateVo.candidate` 为 WebRTC 原始对象。

### 9.3 群通话和 LiveKit

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `POST` | `/v1/api/voip/call/group/invite` | `GroupCallInviteVo` | `CallInviteDto` | `callType` 为 `audio` 或 `video` |
| `POST` | `/v1/api/voip/call/group/hangup` | `GroupCallHangupVo` | 无 | 只有群主可结束整场通话 |
| `POST` | `/v1/api/voip/livekit/host` | 无 | LiveKit Host | 当前实现不要求登录 |
| `POST` | `/v1/api/voip/livekit/token/group` | `LiveKitTokenVo` | Token 字符串 | 群成员获取群房间 Token |
| `POST` | `/v1/api/voip/livekit/room/users` | `LiveKitTokenVo` | 房间用户列表 | 群成员可查询 |
| `POST` | `/v1/api/voip/livekit/token/live/start` | 无 | `LiveResultDto` | 开始直播并获得发布 Token |
| `POST` | `/v1/api/voip/livekit/token/live/get` | `LiveKitTokenVo` | `LiveResultDto` | 观众获得订阅 Token |
| `GET` | `/v1/api/voip/livekit/live/list` | 无 | 直播间列表 | 当前实现不要求登录 |

### 9.4 AI 模型和问答

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/v1/api/ai/model/list` | 无 | 模型配置列表 | 返回对象不包含 `apiKey` |
| `POST` | `/v1/api/ai/model/add` | `AddAiModelVo` | 无 | 新增个人模型配置 |
| `POST` | `/v1/api/ai/model/update` | `UpdateAiModelVo` | 无 | 修改个人模型配置 |
| `POST` | `/v1/api/ai/model/delete` | `DeleteAiModelVo` | 无 | 删除个人模型配置 |
| `GET` | `/v1/api/ai/chat/record/list` | 无 | AI 记录列表 | 按创建时间升序 |
| `POST` | `/v1/api/ai/chat/answers` | `AiChatAnswersVo` | `AiChatRecord` | 同步问答 |
| `POST` | `/v1/api/ai/chat/answers/stream` | `AiChatAnswersVo` | SSE | 流式问答，响应类型 `text/event-stream` |

SSE 事件：

```text
event: delta
data: {"content":"增量文本"}

event: done
data: {"id":"...","role":"assistant","content":"完整回答"}

event: error
data: {"msg":"错误信息"}
```

## 10. 二维码接口

| 方法 | 路径 | 请求 | 返回 `data` | 说明 |
| --- | --- | --- | --- | --- |
| `GET` | `/qr/code` | 设计意图为 `action`、`groupId` 参数 | 二维码 key | `action` 支持 `login`、`mine`、`group` |
| `POST` | `/qr/code/result` | `ResultVo` | `QrCodeResult` | 轮询二维码结果，无需登录 |
| `GET` | `/qr/code/status` | `StatusVo` 请求体 | `QrCodeResult` | 标记已扫码；当前 Controller 使用 GET 携带 JSON body |

二维码登录流程：PC 调用 `/qr/code` 获取 key；移动端扫码后调用 `/v1/api/login/qr` 绑定 key；PC 轮询 `/qr/code/result` 获取 `extend.token`。

> 兼容性提示：当前 `QrCodeController` 对 `action`、`groupId` 使用的是 `org.simpleframework.xml.Path`，不是 Spring MVC 的 `@RequestParam` 或 `@PathVariable`。如果调用 `/qr/code` 时参数无法绑定，需要先将 Controller 注解修正为 Spring MVC 注解。`/qr/code/status` 当前也使用 GET + `@RequestBody`，部分客户端或网关可能丢弃请求体。

## 11. WebSocket 协议

### 11.1 建立连接

```text
ws://{host}:9100/ws?x-token={JWT_TOKEN}
```

Token 只从查询参数 `x-token` 读取。连接成功后，服务端校验 JWT 和账号状态；同一账号建立新连接时，旧连接会收到 `disable` 类型消息并被关闭。连接空闲约 30 秒会触发 Netty 空闲检测。

服务端文本帧统一格式：

```json
{
  "type": "msg",
  "content": {}
}
```

`type` 类型：

| 类型 | 用途 |
| --- | --- |
| `msg` | 普通消息 |
| `notify` | 好友、群组、系统通知 |
| `video` | 单人音视频 WebRTC 信令 |
| `call` | 群通话信令 |
| `system_notify` | 全局系统通知 |
| `disable` | 账号被禁用或被其他设备挤下线 |

单人通话 `content.type` 为 `invite`、`accept`、`offer`、`answer`、`candidate` 或 `hangup`；群通话内容包含 `action`、`sessionId`、`fromUserId`、`groupId`、`toUserIds`、`callType` 和 `sceneType`。

## 12. 管理端 API

管理端登录：

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `POST` | `/admin/v1/api/login` | `LoginVo` | 管理员用户信息和 `token` |

除登录外，管理端业务接口需要管理员 Token。路径 `/admin/v1/api/user/isAdmin` 当前只做 Token 认证，接口自身未标记管理员角色限制。

### 12.1 用户管理

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `GET` | `/admin/v1/api/user/isAdmin` | 无 | 无 |
| `POST` | `/admin/v1/api/user/page` | `UserListVo` | MyBatis `Page<User>` |
| `POST` | `/admin/v1/api/user/create` | `CreateUserVo` | 无 |
| `POST` | `/admin/v1/api/user/update` | `UpdateUserVo` | 无 |
| `POST` | `/admin/v1/api/user/disable` | `DisableUserVo` | 无 |
| `POST` | `/admin/v1/api/user/unDisable` | `UnDisableUserVo` | 无 |
| `POST` | `/admin/v1/api/user/delete` | `DeleteUserVo` | 无 |
| `POST` | `/admin/v1/api/user/reset/password` | `ResetPasswordVo` | 新密码字符串 |
| `POST` | `/admin/v1/api/user/set/admin` | `SetAdminVo` | 无 |
| `POST` | `/admin/v1/api/user/cancel/admin` | `CancelAdminVo` | 无 |

### 12.2 会话与系统通知管理

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `POST` | `/admin/v1/api/conversation/create` | Form `portrait`、`name` | `Conversation` |
| `POST` | `/admin/v1/api/conversation/update` | Form `portrait`、`name`、`id` | 无 |
| `GET` | `/admin/v1/api/conversation/list` | 无 | 会话列表 |
| `POST` | `/admin/v1/api/conversation/delete` | `DeleteConversationVo` | 无 |
| `POST` | `/admin/v1/api/conversation/reset/secret` | `ResetSecretVo` | 无 |
| `POST` | `/admin/v1/api/conversation/disable` | `DisableConversationVo` | 无 |
| `POST` | `/admin/v1/api/conversation/undisable` | `UnDisableConversationVo` | 无 |
| `GET` | `/admin/v1/api/notify/system/list` | 无 | 系统通知列表 |
| `POST` | `/admin/v1/api/notify/system/create` | Form `file`、`title`、`text` | 无 |
| `POST` | `/admin/v1/api/notify/system/delete` | `DeleteNotifyVo` | 无 |

### 12.3 统计

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `POST` | `/admin/v1/api/stat/login/details` | `LoginDetailsVo` | 登录详情列表 |
| `GET` | `/admin/v1/api/stat/num/info` | 无 | 登录数、在线数、消息数和统计列表 |
| `GET` | `/admin/v1/api/stat/top10/msg` | 无 | 消息数量 Top 10 |

## 13. 第三方发送接口

| 方法 | 路径 | 请求 | 返回 `data` |
| --- | --- | --- | --- |
| `POST` | `/v1/api/expose/send` | `ThirdSendMsgVo` | 无 |

该接口不是匿名接口，而是使用会话级 HMAC 签名。请求头：

```http
X-Access-Key: {accessKey}
X-Timestamp: {unix_milliseconds}
X-Signature: {hmac_sha256_hex}
```

签名原文：

```text
HTTP_METHOD + REQUEST_URI + accessKey + timestamp
```

签名算法：

```text
HMAC-SHA256(secretKey, stringToSign)
```

时间戳与服务端时间相差超过 5 分钟会被拒绝。`ThirdSendMsgVo.email` 为目标会话邮箱，`content` 为消息内容。

## 14. 请求对象速查

以下表格列出主要 JSON 请求体字段。带 `*` 的字段由 Bean Validation 标记为必填；未标记字段可能仍会被业务逻辑要求。

### 账号、用户和好友

| 对象 | 字段 |
| --- | --- |
| `RegisterVo` | `username*`、`account*`、`email*`、`code*`、`password*` |
| `LoginVo` | `account*`、`password*`、`onlineEquipment`、`pushyToken` |
| `ForgetVo` | `account*`、`password*`、`code*` |
| `EmailVerifyVo` | `email*` |
| `EmailVerifyByAccountVo` | `account*` |
| `SearchUserVo` | `userInfo*` |
| `UpdateVo` | `name*`、`sex`、`birthday`、`signature`、`portrait*` |
| `UpdatePasswordVo` | `oldPassword`、`newPassword`、`confirmPassword` |
| `SearchVo` | `searchInfo` |
| `AgreeFriendApplyVo` | `notifyId*`、`fromId` |
| `RejectFriendApplyVo` | `fromId*` |
| `SetRemarkVo` | `friendId*`、`remark` |
| `SetGroupVo` | `friendId*`、`groupId` |
| `DeleteFriendVo` / `CareForFriendVo` / `UnCareForFriendVo` | `friendId*` |

### 会话、群组和通知

| 对象 | 字段 |
| --- | --- |
| `CreateChatListVo` | `toId*`、`type`，默认 `user` |
| `DeleteChatListVo` / `TopChatListVo` | `chatListId*`；`TopChatListVo` 另有 `isTop*` |
| `DetailChatListVo` | `targetId`、`type` |
| `CreateChatGroupVo` | `name*`、`notice`、`users[]`（元素含 `userId`、`name`） |
| `UpdateChatGroupVo` | `groupId*`、`updateKey`、`updateValue` |
| `UpdateChatGroupNameVo` | `groupId*`、`name*` |
| `InviteMemberVo` | `groupId*`、`userIds[]` |
| `QuitChatGroupVo` / `DissolveChatGroupVo` | `groupId*` |
| `KickChatGroupVo` / `TransferChatGroupVo` | `groupId*`、`userId*` |
| `AgreeGroupApplyVo` / `RejectGroupApplyVo` | `fromId*`、`groupId*` |
| `DetailsChatGroupVo` | `chatGroupId` |
| `MemberListVo` | `chatGroupId`、`index` 默认 `0`、`num` 默认 `10` |
| `BanMemberVo` | `groupId*`、`targetId*`、`banDuration*`，单位秒，`0` 表示解除 |
| `CreateNoticeVo` | `groupId*`、`content*` |
| `NoticeListVo` | `groupId*` |
| `DeleteNoticeVo` | `groupId*`、`noticeId*` |
| `UpdateNoticeVo` | `groupId*`、`noticeId*`、`noticeContent*` |
| `FriendApplyNotifyVo` | `userId*`、`content` |
| `GroupApplyNotifyVo` | `groupId*`、`content` |
| `ReadNotifyVo` | `notifyType*`：`friend`、`group` 或 `system` |

### 消息、云盘和动态

| 对象 | 字段 |
| --- | --- |
| `SendMsgVo` | `toUserId*`、`source`、`msgContent*`、`isForward`、`fromMsgId` |
| `MsgContent` | `fromUserId`、`fromUserName`、`fromUserPortrait`、`type`、`content`、`ext` |
| `MessageRecordVo` | `targetId`、`index`、`num` |
| `RetractionMsgVo` | `msgId`、`targetId` |
| `ReeditMsgVo` | `msgId` |
| `FileDeleteVo` | `spaceFileIds*`，非空字符串数组 |
| `RecycleRestoreVo` / `RecycleDeleteVo` | `spaceRecycleIds*`，非空字符串数组 |
| `UploadCheckVo` | `fileHash*`、`fileSize*`、`fileName*`、`totalChunk*` |
| `TalkListVo` | `index`、`num`、`targetId` |
| `CreateTalkVo` | `text`、`permission[]` |
| `DetailsTalkVo` / `DeleteTalkVo` | `talkId` |
| `CreateTalkCommentVo` | `talkId`、`comment` |
| `DeleteTalkCommentVo` | `talkId`、`talkCommentId` |
| `TalkCommentListVo` / `CreateTalkLikeVo` / `DeleteTalkLikeVo` / `TalkLikeListVo` | `talkId` |

### AI、直播和通话

| 对象 | 字段 |
| --- | --- |
| `AddAiModelVo` | `modelName*`、`baseUrl*`、`apiKey*`、`model*` |
| `UpdateAiModelVo` | `id*`、`modelName`、`baseUrl`、`apiKey`、`model` |
| `DeleteAiModelVo` | `id*` |
| `AiChatAnswersVo` | `modelId*`、`question*` |
| `UpdateLiveRoomTitleVo` | `title*`，最多 20 字符 |
| `PublishDanmakuVo` | `sessionId*`、`content*`，最多 200 字符 |
| `InviteVo` | `userId`、`isOnlyAudio` |
| `AcceptVo` / `HangupVo` | `userId` |
| `OfferVo` / `AnswerVo` | `userId`、`desc` |
| `CandidateVo` | `userId`、`candidate` |
| `GroupCallInviteVo` | `groupId*`、`userIds[]`、`callType*`：`audio`/`video` |
| `GroupCallHangupVo` | `groupId*` |
| `LiveKitTokenVo` | `sessionId*` |

### 管理端

| 对象 | 字段 |
| --- | --- |
| `UserListVo` | `currentPage` 默认 `1`、`pageSize` 默认 `10`、`keyword`、`onlineStatus` |
| `CreateUserVo` | `account*`、`name*`、`email`、`phone` |
| `UpdateUserVo` | `id*`、`name*`、`email`、`phone` |
| `DisableUserVo` / `DeleteUserVo` / `SetAdminVo` / `CancelAdminVo` | `userId*` |
| `UnDisableUserVo` / `ResetPasswordVo` | `userId` |
| `DeleteConversationVo` / `DisableConversationVo` / `UnDisableConversationVo` / `ResetSecretVo` | `conversationId*` |
| `DeleteNotifyVo` | `notifyId*` |
| `LoginDetailsVo` | `index`、`num`、`keyword` |
| `ThirdSendMsgVo` | `email*`、`content` |

## 15. 维护说明

- 本文档没有记录 `/test` 调试接口。
- 文件临时 URL 通常由 MinIO 生成并缓存 7 天，客户端不应长期持久化而应在需要时重新获取。
- API 路径大小写当前敏感，例如 `/message/send/Img` 中的 `Img` 不应改为 `img`。
- 管理端和第三方接口涉及高权限操作，生产环境应使用环境变量配置密钥，并避免把密钥写入前端或日志。
