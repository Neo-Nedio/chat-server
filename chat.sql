create table chat_group
(
    id                varchar(64)          not null
        primary key,
    user_id           varchar(64)          not null comment '创建用户id',
    owner_user_id     varchar(64)          not null comment '群主id',
    portrait          text                 null comment '群头像',
    name              varchar(64)          null comment '群名名称',
    notice            text                 null comment '群公告',
    member_num        int        default 0 null comment '成员数',
    status            tinyint(1) default 1 not null comment '状态：0-已解散，1-正常',
    create_time       timestamp(3)         not null comment '创建时间',
    update_time       timestamp(3)         not null comment '更新时间',
    chat_group_number varchar(64)          not null comment '群号'
)
    comment '聊天群表' row_format = DYNAMIC;

create table chat_group_member
(
    id                    varchar(64)  not null
        primary key,
    chat_group_id         varchar(64)  not null comment '聊天群id',
    user_id               varchar(64)  not null comment '成员id',
    group_remark          varchar(64)  null comment '群备注',
    group_name            varchar(64)  null comment '群昵称',
    create_time           timestamp(3) not null comment '创建时间',
    update_time           timestamp(3) not null comment '更新时间',
    last_read_notice_time timestamp(3) null comment '上次群通知已读时间',
    ban_end_time          timestamp(3) null comment '禁言截止时间，NULL表示未禁言'
)
    comment '聊天群成员表' row_format = DYNAMIC;

create table chat_group_notice
(
    id             varchar(64)  not null
        primary key,
    chat_group_id  varchar(64)  not null comment '聊天群id',
    user_id        varchar(64)  not null comment '成员id',
    notice_content text         null comment '公告内容',
    create_time    timestamp(3) not null comment '创建时间',
    update_time    timestamp(3) not null comment '更新时间'
)
    comment '聊天群公告表' row_format = DYNAMIC;

create table chat_list
(
    id               varchar(64)      not null
        primary key,
    user_id          varchar(64)      not null comment '用户id',
    from_id          varchar(64)      not null comment '会话目标id',
    is_top           bit default b'0' null comment '是否置顶',
    unread_num       int default 0    null comment '未读消息数量',
    last_msg_content text             null comment '最后消息内容',
    type             varchar(64)      null comment '类型',
    status           varchar(500)     null comment '状态',
    create_time      timestamp(3)     not null comment '创建时间',
    update_time      timestamp(3)     not null comment '更新时间'
)
    comment '聊天列表' row_format = DYNAMIC;

create table conversation
(
    id          varchar(64)  not null
        primary key,
    user_id     varchar(64)  not null comment '用户id',
    access_key  varchar(128) not null comment 'access key',
    secret_key  varchar(128) not null comment 'secret_key',
    status      varchar(128) not null comment '状态',
    create_time timestamp(3) not null comment '创建时间',
    update_time timestamp(3) not null comment '更新时间'
)
    comment '会话表' row_format = DYNAMIC;

create table emoji
(
    id          varchar(64)  not null comment '主键ID'
        primary key,
    user_id     varchar(64)  not null comment '用户id',
    emoji       text         null comment '表情',
    create_time timestamp(3) not null comment '创建时间',
    update_time timestamp(3) not null comment '更新时间'
)
    comment '表情包表' row_format = DYNAMIC;

create table friend
(
    id          varchar(64)              not null
        primary key,
    user_id     varchar(64)              not null comment '用户id',
    friend_id   varchar(64)              not null comment '好友id',
    remark      varchar(64)              null comment '备注',
    group_id    varchar(64) default '0'  null comment '分组id',
    is_back     bit         default b'0' null comment '是否拉黑',
    is_concern  bit         default b'0' null comment '是否特别关心',
    status      varchar(500)             null comment '状态',
    create_time timestamp(3)             not null comment '创建时间',
    update_time timestamp(3)             not null comment '更新时间'
)
    comment '好友表' row_format = DYNAMIC;

create table `group`
(
    id              varchar(64)  not null
        primary key,
    user_id         varchar(64)  not null comment '用户id',
    name            varchar(64)  null comment '分组名称',
    parent_group_id varchar(64)  null comment '父分组id',
    create_time     timestamp(3) not null comment '创建时间',
    update_time     timestamp(3) not null comment '更新时间'
)
    comment '分组表' row_format = DYNAMIC;

create table message
(
    id                 varchar(64)      not null
        primary key,
    from_id            varchar(64)      not null comment '消息发送方id',
    to_id              varchar(64)      not null comment '消息接受方id',
    type               varchar(64)      null comment '消息类型',
    is_show_time       bit default b'0' null comment '是否显示时间',
    msg_content        text             null comment '消息内容',
    status             varchar(500)     null comment '消息状态',
    source             varchar(64)      not null comment '消息源',
    create_time        timestamp(3)     not null comment '创建时间',
    update_time        timestamp(3)     not null comment '更新时间',
    from_forward_msgId varchar(64)      null comment '转发消息的id'
)
    comment '消息表' row_format = DYNAMIC;

create table message_retraction
(
    id          varchar(64)  not null
        primary key,
    msg_id      varchar(64)  not null comment '消息id',
    msg_content text         null comment '消息内容',
    create_time timestamp(3) not null comment '创建时间',
    update_time timestamp(3) not null comment '更新时间'
)
    comment '消息撤回内容表' row_format = DYNAMIC;

create table notify
(
    id          varchar(64)  not null
        primary key,
    from_id     varchar(64)  not null comment '发送方',
    to_id       varchar(64)  not null comment '目标方',
    type        varchar(64)  null comment '类型',
    status      varchar(64)  null comment '状态',
    content     text         null comment '通知内容',
    unread_id   varchar(128) null comment '未读方',
    create_time timestamp(3) not null comment '创建时间',
    update_time timestamp(3) not null comment '更新时间'
)
    comment '通知' row_format = DYNAMIC;

create table statistic
(
    id          varchar(64)   not null
        primary key,
    date        date          not null comment '日期',
    login_num   int default 0 null comment '登录数量',
    online_num  int default 0 null comment '在线数量',
    create_time timestamp(3)  not null comment '创建时间',
    update_time timestamp(3)  not null comment '更新时间'
)
    comment '统计表' row_format = DYNAMIC;

create table talk
(
    id             varchar(64)   not null
        primary key,
    user_id        varchar(64)   not null comment '用户id',
    content        text          null comment '说说内容',
    like_num       int default 0 null comment '点赞数量',
    comment_num    int default 0 null comment '评论数量',
    latest_comment text          null comment '最近的评论内容',
    status         varchar(64)   null comment '状态',
    create_time    timestamp(3)  not null comment '创建时间',
    update_time    timestamp(3)  not null comment '更新时间'
)
    comment '说说' row_format = DYNAMIC;

create table talk_comment
(
    id          varchar(64)  not null
        primary key,
    talk_id     varchar(64)  not null comment '说说id',
    user_id     varchar(64)  not null comment '用户id',
    content     text         null comment '评论内容',
    status      varchar(64)  null comment '状态',
    create_time timestamp(3) not null comment '创建时间',
    update_time timestamp(3) not null comment '更新时间'
)
    comment '说说评论' row_format = DYNAMIC;

create table talk_like
(
    id          varchar(64)  not null
        primary key,
    talk_id     varchar(64)  not null comment '说说id',
    user_id     varchar(64)  not null comment '用户id',
    status      varchar(64)  null comment '状态',
    create_time timestamp(3) not null comment '创建时间',
    update_time timestamp(3) not null comment '更新时间'
)
    comment '说说点赞' row_format = DYNAMIC;

create table talk_permission
(
    id          varchar(64)               not null
        primary key,
    talk_id     varchar(64)               not null comment '说说id',
    permission  varchar(64) default 'all' null comment '权限:用户id,all',
    status      varchar(64)               null comment '状态',
    create_time timestamp(3)              not null comment '创建时间',
    update_time timestamp(3)              not null comment '更新时间'
)
    comment '说说查看权限' row_format = DYNAMIC;

create table user
(
    id               varchar(64)      not null
        primary key,
    account          varchar(64)      not null comment '用户账号',
    name             varchar(200)     not null comment '用户名',
    portrait         text             null comment '头像',
    password         varchar(200)     not null comment '密码',
    sex              varchar(64)      null comment '性别',
    birthday         timestamp(3)     null comment '生日',
    signature        text             null comment '签名',
    phone            varchar(64)      null comment '手机号',
    email            varchar(200)     null comment '邮箱',
    last_opt_time    timestamp(3)     null comment '最后操作时间',
    role             varchar(64)      null comment '用户角色',
    status           varchar(500)     null comment '用户状态',
    is_online        bit default b'0' null comment '是否在线',
    create_time      timestamp(3)     not null comment '创建时间',
    update_time      timestamp(3)     not null comment '更新时间',
    online_equipment varchar(20)      null comment '在线设备',
    chat_background  varchar(255)     null comment '聊天背景',
    notify_read_time datetime         null comment '系统通知已读时间'
)
    comment '用户表' row_format = DYNAMIC;

create table live_room
(
    id                varchar(64)  not null primary key,
    user_id           varchar(64)  not null comment '主播用户id',
    title             varchar(255) not null default '这个人太懒，还没有给直播间起标题' comment '直播间标题',
    background        varchar(255) null comment '直播间背景图片',
    create_time       timestamp(3) not null comment '创建时间',
    update_time       timestamp(3) not null comment '更新时间',
    unique key uk_live_room_user_id (user_id)
)
    comment '直播间信息表' row_format = DYNAMIC;

create table user_operated
(
    id          varchar(64)  not null
        primary key,
    user_id     varchar(64)  not null comment '用户id',
    type        varchar(64)  null comment '操作类型',
    content     text         null comment '操作内容',
    create_time timestamp(3) not null comment '创建时间',
    update_time timestamp(3) not null comment '更新时间'
)
    comment '用户操作表' row_format = DYNAMIC;

create table user_set
(
    id          varchar(64)  not null
        primary key,
    user_id     varchar(64)  not null comment '用户id',
    sets        text         null comment '用户设置',
    create_time timestamp(3) not null comment '创建时间',
    update_time timestamp(3) not null comment '更新时间'
)
    comment '用户设置表' row_format = DYNAMIC;

create table ai_model
(
    id          varchar(64)  not null
        primary key,
    user_id     varchar(64)  not null comment '归属用户id',
    model_name  varchar(255) not null comment '显示名（用户自定义）',
    base_url    varchar(512) not null comment 'OpenAI兼容接口地址',
    api_key     varchar(255) not null comment '模型ApiKey',
    model       varchar(255) not null comment '模型标识',
    create_time timestamp(3) not null comment '创建时间',
    update_time timestamp(3) not null comment '更新时间',
    key idx_user_id (user_id)
)
    comment '用户AI模型配置表' row_format = DYNAMIC;

create table ai_chat_record
(
    id          varchar(64) not null
        primary key,
    user_id     varchar(64) not null comment '归属用户id',
    model_id    varchar(64) not null comment '使用的模型配置id（仅记录，查询不按模型过滤）',
    role        varchar(32) not null comment '角色：user/assistant',
    content     text        null comment '消息文本',
    create_time timestamp(3) not null comment '创建时间',
    key idx_user_id (user_id)
)
    comment 'AI聊天记录表' row_format = DYNAMIC;

create table space
(
    id          varchar(64)       not null
        primary key,
    user_id     varchar(64)       not null comment '用户id（一人一空间）',
    quota_bytes bigint default 0  not null comment '空间总容量（字节），0表示不限制',
    used_bytes  bigint default 0  not null comment '已使用容量（字节）',
    file_count  bigint default 0  not null comment '文件数量',
    create_time timestamp(3)      not null comment '创建时间',
    update_time timestamp(3)      not null comment '更新时间',
    constraint uk_space_user unique (user_id)
)
    comment '云盘空间表' row_format = DYNAMIC;

create table space_file
(
    id            varchar(64)      not null
        primary key,
    space_id      varchar(64)      not null comment '空间id',
    physical_id   varchar(64)      not null comment '物理文件id',
    file_name     varchar(255)     not null comment '文件名称',
    file_category varchar(32)      not null comment '文件分类：image/video/document/audio/archive',
    file_size     bigint default 0 not null comment '文件大小（字节）',
    deleted       tinyint(1) default 0 not null comment '逻辑删除：0-正常，1-已删除（回收站中）',
    create_time   timestamp(3)     not null comment '上传时间',
    update_time   timestamp(3)     not null comment '更新时间',
    key idx_space_file_space (space_id),
    key idx_space_file_category (space_id, file_category)
)
    comment '云盘文件表（个人云盘，文件平铺无目录）' row_format = DYNAMIC;

create table space_recycle
(
    id            varchar(64)  not null
        primary key,
    user_id       varchar(64)  not null comment '所属用户id',
    space_id      varchar(64)  not null comment '空间id',
    space_file_id varchar(64)  not null comment '文件id',
    expire_at     timestamp(3) null comment '过期时间，到期可彻底删除',
    create_time   timestamp(3) not null comment '入回收站时间',
    update_time   timestamp(3) not null comment '更新时间',
    constraint uk_space_recycle_file unique (space_file_id),
    key idx_space_recycle_user (user_id, space_id)
)
    comment '云盘回收站表' row_format = DYNAMIC;

create table physical_file
(
    id           varchar(64)         not null
        primary key,
    file_hash    varchar(64)         not null comment '文件hash',
    file_size    bigint              not null comment '文件大小（字节）',
    storage_path text                not null comment '存储路径（MinIO对象名）',
    ref_count    int     default 0   not null comment '引用计数',
    create_time  timestamp(3)        not null comment '创建时间',
    update_time  timestamp(3)        not null comment '更新时间',
    constraint uk_physical_file_hash unique (file_hash)
)
    comment '云盘物理文件表' row_format = DYNAMIC;
