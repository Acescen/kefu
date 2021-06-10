<template>
    <div class="app-container" :style="'height:' + (clientHeight - 50) + 'px;'">
        <!-- 左边栏 -->
        <div class="left-container">
            <el-table
                    :data="conversationList"
                    style="width: 100%"
                    max-height="700"
                    highlight-current-row
                    @cell-click="handleConversation"
            >
                <el-table-column label="所有聊天会话记录">
                    <template slot-scope="{row}">
                        <span>{{ row.toUser.nickname +' <~~> '+ row.fromUser.nickname}}</span>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 中间栏 -->
        <div class="center-container">
            <div v-if="conversation" class="chat-container">
                <!-- 导航栏 -->
                <div class="chat-nav">
                    <span v-text="toUser.nickname +' <~~> '+ fromUser.nickname"/>
                </div>

                <!-- 可上下滑滚动区域 -->
                <div id="scrollLoader-container" class="scroll-container">
                    <div v-if="topLoading" class="loading">
                        <div class="loader">加载历史记录...</div>
                    </div>

                    <!-- 消息内容列表容器 -->
                    <div class="message-container">
                        <!-- 消息内容列表 -->
                        <div v-if="messageList && messageList.length > 0" class="message">
                            <ul>
                                <li
                                        v-for="message in messageList"
                                        :key="message.id"
                                        :class="isToUser(message) ? 'an-move-right' : 'an-move-left'"
                                >
                                    <!-- 时间 -->
                                    <div class="time"><span v-text="message.createdAt"/></div>

                                    <!-- 系统提示 -->
                                    <div v-if="message.type === '10000'" class="time system">
                                        <span v-html="message.content"/>
                                    </div>
                                    <div v-else :class="'main' + (isToUser(message) ? ' self' : '')">
                                        <!-- 头像 -->
                                        <img
                                                class="avatar"
                                                :src="isToUser(message) ? getImgUrl(fromUser.avatar) : getImgUrl(toUser.avatar)"
                                                alt="头像图片"
                                        >
                                        <!-- 文本 -->
                                        <div v-if="message.type === '1'" v-emotion="message.content" class="text"/>

                                        <!-- 图片 -->
                                        <div v-else-if="message.type === '2'" class="text">
                                            <img :src="message.content" class="image" alt="聊天图片">
                                        </div>

                                        <!-- 其他 -->
                                        <div
                                                v-else
                                                class="text"
                                                v-text="'[暂未支持的消息类型:' + message.type + ']\n\r' + message.content"
                                        />
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
            <div v-else style="flex: 1;">
                <span style="margin-top: 250px; display: block; text-align: center">点击会话查看消息</span>
            </div>
        </div>
    </div>
</template>

<script>
    import conversationApi from '@/api/conversation'
    import messageApi from '@/api/message'

    export default {
        data() {
            return {
                messageList: [], // 聊天信息列表
                conversationList: [], // 会话列表
                conversation: null, // 当前选中的会话
                listQuery: {
                    userId: 0,
                    contactUserId: 0,
                    lessMessageId: 0
                }, // 列表查询条件

                topLoading: false,
                stopTopLoading: false, // 是否停止传播滚动到顶部事件
                isUpperLaoding: false,
                isRefreshedAll: false,

                clientHeight: window.innerHeight, // 浏览器高度

                // inputText: '', // 输入的文本内容

                socket: null, // socket
                eventDispatcher: null, // 事件派发器
                interval: null, // 间隔执行定时器

                fromUser: null, // 接收用户对象
                toUser: null // 发送用户对象
            }
        },
        // computed: {},
        // 不能操作DOM
        created() {
            this.getAllConversationList()
        },
        methods: {
            getImgUrl(avatar) {
                return require("@/assets/avator/" + avatar);
            },
            // 滚动到聊天框底部
            scrollToBottom() {
                const _this = this
                this.$nextTick(() => {
                    const scrollContainer = _this.$el.querySelector('#scrollLoader-container')
                    scrollContainer.scrollTop = scrollContainer.scrollHeight - scrollContainer.clientHeight
                })
            },
            // 获取会话列表
            getAllConversationList() {
                conversationApi.getAllConversationList().then((response) => {
                    if (response.status === 200) {
                        this.conversationList = response.data
                        console.log(response.data)
                    }
                })
            },
            // 获取聊天信息列表
            getMessageList(done) {
                const _this = this
                messageApi.getMessageList(this.listQuery).then((response) => {
                    if (response.status === 200) {
                        _this.messageList = response.data.reverse().concat(_this.messageList) // 倒序合并
                        // console.log(response.data)
                        _this.isUpperLaoding = false
                    }
                })
            },

            // 是否是自己发送的信息
            isToUser(message) {
                return message.toUserId === this.toUser.id
            },
            // 选择会话
            handleConversation(conversation) {
                this.conversation = conversation
                this.toUser = conversation.toUser
                this.fromUser = conversation.fromUser

                this.messageList = []
                this.listQuery.userId = this.toUser.id
                this.listQuery.contactUserId = this.fromUser.id
                this.listQuery.lessMessageId = 0

                this.getMessageList()
            },
        }
    }
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
    ::v-deep .el-textarea__inner {
        border: 0;
        resize: none;
    }

    ul {
        display: block;
        list-style-type: disc;
        margin-block-start: 1em;
        margin-block-end: 1em;
        margin-inline-start: 0;
        margin-inline-end: 0;
        padding-inline-start: 0;
    }
</style>

<style scoped>
    .app-container {
        background-color: #ffffff;
        z-index: 100;
        overflow: hidden;
        min-width: 400px;
        margin: 0 auto;
        padding: 0;
        display: flex;
        display: -webkit-flex;
        flex-direction: row;
    }

    .left-container {
        width: 250px;
    }

    .center-container {
        flex: 1;
    }

    .chat-container {
        display: flex;
        display: -webkit-flex;
        flex-direction: column;
        height: 100%;
        border-left: 1px solid #EBEEF5;
        border-right: 1px solid #EBEEF5;
    }

    .chat-nav {
        text-align: center;
        width: 100%;
        height: 50px;
        line-height: 50px;
        font-size: 14px;
        border-bottom: 1px solid #EBEEF5;
    }

    .input-content .input-send {
        width: 80px;
        display: flex;
        display: -webkit-flex;
        flex-direction: column;
        align-self: flex-end;
    }

    .input-send .input-send-btn {
        margin: 0 15px 15px 0;
    }

    .input-tool-bar i {
        width: 35px;
        text-align: center;
        font-size: 1.5em;
        color: #AAB2BC;
    }

    .message {
        padding: 10px 15px;
    }

    .message li {
        margin-bottom: 15px;
        left: 0;
        position: relative;
        display: block;
    }

    .message .time {
        margin: 10px 0;
        text-align: center;
    }

    .message .text {
        display: inline-block;
        position: relative;
        max-width: calc(100% - 75px);
        min-height: 35px;
        line-height: 2.1;
        font-size: 15px;
        padding: 6px 10px;
        text-align: left;
        word-break: break-all;
        background-color: #fff;
        color: #000;
        border-radius: 4px;
        box-shadow: 0 1px 7px -5px #000;
    }

    .message .avatar {
        float: left;
        margin: 0 10px 0 0;
        border-radius: 3px;
        background: #fff;
        width: 45px;
        height: 45px;
    }

    .message .time > span {
        display: inline-block;
        padding: 0 5px;
        font-size: 12px;
        color: #fff;
        border-radius: 2px;
        background-color: #dadada;
    }

    .message .system > span {
        padding: 4px 9px;
        text-align: left;
    }

    .message .text:before {
        content: " ";
        position: absolute;
        top: 9px;
        right: 100%;
        border: 6px solid transparent;
        border-right-color: #fff;
    }

    .message .main {
        text-align: left;
    }

    .message .self {
        text-align: right;
    }

    .message .self .avatar {
        float: right;
        margin: 0 0 0 10px;
    }

    .message .self .text {
        background-color: #9eea6a;
    }

    .message .self .text:before {
        right: inherit;
        left: 100%;
        border-right-color: transparent;
        border-left-color: #9eea6a;
    }

    .message .image {
        max-width: 200px;
    }

    @keyframes moveRight {
        0% {
            left: -20px;
            opacity: 0;
        }
        100% {
            left: 0;
            opacity: 1;
        }
    }

    @-webkit-keyframes moveRight {
        0% {
            left: -20px;
            opacity: 0;
        }
        100% {
            left: 0;
            opacity: 1;
        }
    }

    @keyframes moveLeft {
        0% {
            left: 20px;
            opacity: 0;
        }
        100% {
            left: 0;
            opacity: 1;
        }
    }

    @-webkit-keyframes moveLeft {
        0% {
            left: 20px;
            opacity: 0;
        }
        100% {
            left: 0;
            opacity: 1;
        }
    }

    @media (max-width: 367px) {
        .fzDInfo {
            width: 82%;
        }
    }

    .scroll-container {
        margin: 0 auto;
        overflow: auto;
        overflow-x: hidden;
        padding: 0;
        flex: 1;
        width: 100%;
    }

    .message-container {
        overflow-x: hidden;
        flex: 1;
        width: 100%;
    }

    .loading {
        width: 100%;
        height: 40px;
        position: relative;
        overflow: hidden;
        text-align: center;
        margin: 5px 0;
        font-size: 13px;
        color: #b0b0b0;
        line-height: 100px;
    }

    .loader {
        font-size: 10px;
        margin: 8px auto;
        text-indent: -9999em;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: #999;
        background: -moz-linear-gradient(left, #999 10%, rgba(255, 255, 255, 0) 42%);
        background: -webkit-linear-gradient(left, #999 10%, rgba(255, 255, 255, 0) 42%);
        background: -o-linear-gradient(left, #999 10%, rgba(255, 255, 255, 0) 42%);
        background: -ms-linear-gradient(left, #999 10%, rgba(255, 255, 255, 0) 42%);
        background: linear-gradient(to right, #999 10%, rgba(255, 255, 255, 0) 42%);
        position: relative;
        -webkit-animation: load3 1s infinite linear;
        animation: load3 1s infinite linear;
    }

    .loader:before {
        width: 50%;
        height: 50%;
        background: #999;
        border-radius: 100% 0 0 0;
        position: absolute;
        top: 0;
        left: 0;
        content: "";
    }

    .loader:after {
        background: #f5f5f5;
        width: 72%;
        height: 75%;
        border-radius: 68%;
        content: "";
        margin: auto;
        position: absolute;
        top: 0;
        left: 0;
        bottom: 0;
        right: 0;
    }

    @-webkit-keyframes load3 {
        0% {
            -webkit-transform: rotate(0deg);
            transform: rotate(0deg);
        }
        100% {
            -webkit-transform: rotate(360deg);
            transform: rotate(360deg);
        }
    }

    @keyframes load3 {
        0% {
            -webkit-transform: rotate(0deg);
            transform: rotate(0deg);
        }
        100% {
            -webkit-transform: rotate(360deg);
            transform: rotate(360deg);
        }
    }
</style>
