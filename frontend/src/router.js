
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import BorrowBorrowManager from "./components/listers/BorrowBorrowCards"
import BorrowBorrowDetail from "./components/listers/BorrowBorrowDetail"


import ReturnReturnManager from "./components/listers/ReturnReturnCards"
import ReturnReturnDetail from "./components/listers/ReturnReturnDetail"

import BookBookManager from "./components/listers/BookBookCards"
import BookBookDetail from "./components/listers/BookBookDetail"


export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/borrows/borrows',
                name: 'BorrowBorrowManager',
                component: BorrowBorrowManager
            },
            {
                path: '/borrows/borrows/:id',
                name: 'BorrowBorrowDetail',
                component: BorrowBorrowDetail
            },


            {
                path: '/returns/returns',
                name: 'ReturnReturnManager',
                component: ReturnReturnManager
            },
            {
                path: '/returns/returns/:id',
                name: 'ReturnReturnDetail',
                component: ReturnReturnDetail
            },

            {
                path: '/books/books',
                name: 'BookBookManager',
                component: BookBookManager
            },
            {
                path: '/books/books/:id',
                name: 'BookBookDetail',
                component: BookBookDetail
            },



    ]
})
