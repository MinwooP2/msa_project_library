<template>

    <v-data-table
        :headers="headers"
        :items="searchBookList"
        :items-per-page="5"
        class="elevation-1"
    ></v-data-table>

</template>

<script>
    const axios = require('axios').default;

    export default {
        name: 'SearchBookListView',
        props: {
            value: Object,
            editMode: Boolean,
            isNew: Boolean
        },
        data: () => ({
            headers: [
                { text: "id", value: "id" },
            ],
            searchBookList : [],
        }),
          async created() {
            var temp = await axios.get(axios.fixUrl('/searchBookLists'))

            temp.data._embedded.searchBookLists.map(obj => obj.id=obj._links.self.href.split("/")[obj._links.self.href.split("/").length - 1])

            this.searchBookList = temp.data._embedded.searchBookLists;
        },
        methods: {
        }
    }
</script>

