async function login() {

    fetch("/login", {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => {

            return response.json();
        })
        .then((data) => {
            localStorage.setItem("accessToken", data.data);
            alert(JSON.stringify(data));
            // saveToken(data)
        });
}


async function hello() {

    fetch("/hello", {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + getToken(),
        },
    })
        .then((response) => {
            token = response.headers["Authorization"]
            console.log(token);
            if (token != null) {
                localStorage.setItem("accessToken", token);
            }
            return response.json();
        }).then((data) => {
        alert(JSON.stringify(data));
        // saveToken(data)
    });
}

function saveToken(token) {
    console.log(token)
    localStorage.setItem("accessToken", token.data);
    // sessionStorage.setItem("accessToken", JSON.stringify(token));
    // sessionStorage.setItem("refreshToken", JSON.stringify(token.refreshToken));
}

function getToken() {
    token = localStorage.getItem("accessToken");
    console.log(token)
    return token
    // sessionStorage.setItem("accessToken", JSON.stringify(token));
    // sessionStorage.setItem("refreshToken", JSON.stringify(token.refreshToken));
}
