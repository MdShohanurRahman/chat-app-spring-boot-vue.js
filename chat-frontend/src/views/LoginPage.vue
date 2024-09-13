<template>
    <div class="login-container d-flex align-items-center justify-content-center">
        <div class="login-box shadow p-4 rounded">
            <h2>Login</h2>
            <form @submit.prevent="submit">
                <div class="mb-3">
                    <input
                        type="text"
                        class="form-control"
                        placeholder="Enter your username"
                        v-model="username"
                        required
                    />
                </div>
                <button type="submit" class="btn btn-primary w-100">Login</button>
            </form>
        </div>
    </div>
</template>

<script>
import {login} from "@/service/http-client-service";

export default {
    data() {
        return {
            username: ''
        };
    },
    methods: {
        submit() {
            login(this.username)
                .then(response => {
                    localStorage.setItem('user', JSON.stringify(response?.data));
                    this.$router.push('/');
                })
                .catch(error => {
                    console.log(error)
                })

        }
    }
};
</script>

<style scoped>
.login-container {
    height: 100vh;
    background-color: #f8f9fa;
}
.login-box {
    width: 400px;
    background: white;
}
</style>
