import { createRouter, createWebHistory}  from "vue-router";
import LoginPage from "@/views/LoginPage.vue";
import HomePage from "@/views/HomePage.vue";


const routes = [
    {
        path: '/',
        name: 'Home',
        component: HomePage,
        meta: { requiresAuth: true }
    },
    {
        path: '/login',
        name: 'Login',
        component: LoginPage
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})


router.beforeEach((to, from, next) => {
    if (to.matched.some((record) =>  record.meta.requiresAuth)) {
        const user = localStorage.getItem('user');
        if (!user) {
            next({ name: 'Login' });
        } else {
            next();
        }
    } else {
        next();
    }
})

export default router;