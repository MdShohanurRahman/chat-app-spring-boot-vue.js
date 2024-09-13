<template>
    <div class="chat-loader d-flex justify-content-center align-items-center h-100">
        <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
        </div>
    </div>
</template>

<style scoped>
.chat-loader {
    height: 100%;
    position: relative;
}

.spinner-border {
    width: 3rem;
    height: 3rem;
}
</style>
