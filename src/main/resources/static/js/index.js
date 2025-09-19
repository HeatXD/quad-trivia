const TriviaState = {
    difficulty: "all",
    category: 0,
    questions: [],
    currentQuestionIndex: 0,
};

const fetchCategories = async () => {
    try {
        const response = await axios.get("/trivia/categories");
        buildCategories(response.data.trivia_categories || []);
    } catch (err) {
        console.error("Failed to fetch categories:", err);
    }
};

const fetchQuestions = async (difficulty, category) => {
    try {
        const response = await axios.get("/trivia/questions", {
            params: {
                amount: 15,
                category,
                difficulty: difficulty === "all" ? "" : difficulty,
            },
        });

        const newQuestions = response.data.questions || [];
        if (!newQuestions.length) {
            showMessage("No questions available for these settings.");
            return;
        }

        // Append or replace questions depending on current index
        if (TriviaState.currentQuestionIndex >= TriviaState.questions.length) {
            TriviaState.questions = newQuestions;
            TriviaState.currentQuestionIndex = 0;
        } else {
            TriviaState.questions.push(...newQuestions);
        }

        renderQuestion(TriviaState.questions[TriviaState.currentQuestionIndex]);
    } catch (err) {
        console.error("Failed to fetch questions:", err);
    }
};

const renderQuestion = (question) => {
    const triviaDiv = document.querySelector(".trivia > div");
    triviaDiv.innerHTML = "";

    const info = document.createElement("p");
    info.textContent = `${decodeHTML(question.category)} — Difficulty: ${decodeHTML(question.difficulty)}`;
    triviaDiv.appendChild(info);

    triviaDiv.appendChild(document.createElement("br"));

    const h2 = document.createElement("h2");
    h2.textContent = decodeHTML(question.question);
    triviaDiv.appendChild(h2);

    triviaDiv.appendChild(document.createElement("br"));

    const ul = document.createElement("ul");
    ul.classList.add("button-list");

    question.answers.forEach((answer) => {
        const li = document.createElement("li");
        const btn = document.createElement("button");
        btn.textContent = decodeHTML(answer);
        btn.classList.add("trivia-button");
        btn.onclick = () => handleAnswer(btn, answer, question);
        li.appendChild(btn);
        ul.appendChild(li);
    });

    triviaDiv.appendChild(ul);
};

const showMessage = (message) => {
    const triviaDiv = document.querySelector(".trivia > div");
    triviaDiv.innerHTML = `<p>${message}</p>`;
};

const handleAnswer = (button, selectedAnswer, question) => {
    //console.log("Selected answer:", selectedAnswer, "Token:", question.token, "Instant:", question.instant);
    validateAnswer(button, selectedAnswer, question);
};

const validateAnswer = async (button, selectedAnswer, question) => {
    if (button.classList.contains("correct") || button.classList.contains("incorrect")) return;
    try {
        const response = await axios.get("/trivia/validate", {
            params: {
                token: question.token,
                instant: question.instant,
                answer: selectedAnswer,
            },
        });

        if (response.data === true) {
            button.classList.add("correct");
        } else {
            button.classList.add("incorrect");
        }

    } catch (err) {
        console.error("Failed to validate answer:", err);
    }
}

const nextQuestion = () => {
    TriviaState.currentQuestionIndex++;
    if (TriviaState.currentQuestionIndex < TriviaState.questions.length) {
        renderQuestion(TriviaState.questions[TriviaState.currentQuestionIndex]);
    } else {
        fetchQuestions(TriviaState.difficulty, TriviaState.category);
    }
};

const changeDifficulty = (button, difficulty) => {
    TriviaState.difficulty = difficulty;
    toggleActive(button, ".difficulty .nav-button");
};

const changeCategory = (button, category) => {
    TriviaState.category = category;
    toggleActive(button, ".category .nav-button");
};

const toggleActive = (button, selector) => {
    document.querySelectorAll(selector).forEach((btn) => btn.classList.remove("active"));
    button.classList.add("active");
};

const decodeHTML = (html) => {
    const txt = document.createElement("textarea");
    txt.innerHTML = html;
    return txt.value;
};

const startTrivia = (button) => {
    disableControls(true);
    fetchQuestions(TriviaState.difficulty, TriviaState.category);
    button.disabled = true;
    document.querySelector(".trivia > button").classList.remove("hidden");
};

const resetTrivia = () => {
    disableControls(false);
    TriviaState.questions = [];
    TriviaState.currentQuestionIndex = 0;

    document.querySelector(".trivia > div").innerHTML =
        "<h2>Trivia Questions</h2><p>Select your settings and then press start!</p>";

    document.querySelector(".trivia > button").classList.add("hidden");
};

const disableControls = (disable) => {
    document.querySelectorAll(".category .nav-button, .difficulty .nav-button, .header-btn.start")
        .forEach((btn) => (btn.disabled = disable));
};

const buildCategories = (categories) => {
    const ul = document.querySelector(".category .button-list");
    ul.innerHTML = "";
    categories.unshift({ id: 0, name: "All" });

    categories.forEach((cat, index) => {
        const li = document.createElement("li");
        const btn = document.createElement("button");
        btn.textContent = cat.name.includes(":") ? cat.name.split(":")[1].trim() : cat.name;
        btn.classList.add("nav-button");
        if (index === 0) btn.classList.add("active");
        btn.onclick = () => changeCategory(btn, cat.id);
        li.appendChild(btn);
        ul.appendChild(li);
    });
};

document.addEventListener("DOMContentLoaded", () => {
    fetchCategories();
});
