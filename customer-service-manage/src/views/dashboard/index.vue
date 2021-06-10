<template>
    <div class="center" :style="'background-image:url('+ Background +');'">
        <div class="text">
            <p>欢迎登录在线客服系统</p>
        </div>
    </div>
</template>

<script>
    import Background from '@/assets/a.jpg'

    export default {
        data() {
            return {
                Background: Background
            }
        }
    };
</script>

<style>
    .center {
        width: 100%;
        height: 704px;
        /*padding: 10px;*/
        /*border: 1px solid #ccc;*/
        color:#fff;
        font-size: 50px;
        background-size: cover;


        display: -webkit-box;
        -webkit-box-orient: horizontal;
        -webkit-box-pack: center;
        -webkit-box-align: center;

        display: -moz-box;
        -moz-box-orient: horizontal;
        -moz-box-pack: center;
        -moz-box-align: center;

    }

</style>

