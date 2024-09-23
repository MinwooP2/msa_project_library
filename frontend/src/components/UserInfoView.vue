<template>

    <v-data-table
        :headers="headers"
        :items="userInfo"
        :items-per-page="5"
        class="elevation-1"
    ></v-data-table>

</template>

<script>
    const axios = require('axios').default;

    export default {
        name: 'UserInfoView',
        props: {
            value: Object,
            editMode: Boolean,
            isNew: Boolean
        },
        data: () => ({
            headers: [
                { text: "userId", value: "userId" },
                { text: "expireFee", value: "expireFee" },
                { text: "borrowCnt", value: "borrowCnt" },
            ],
            userInfo : [],
        }),
          async created() {
            var temp = await axios.get(axios.fixUrl('/userInfos'))

            temp.data._embedded.userInfos.map(obj => obj.id=obj._links.self.href.split("/")[obj._links.self.href.split("/").length - 1])

            this.userInfo = temp.data._embedded.userInfos;
        },
        methods: {
        }
    }
</script>

