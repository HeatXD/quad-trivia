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
        btn.onclick = () => handleAnswer(answer, question);
        li.appendChild(btn);
        ul.appendChild(li);
    });

    triviaDiv.appendChild(ul);
};

const showMessage = (message) => {
    const triviaDiv = document.querySelector(".trivia > div");
    triviaDiv.innerHTML = `<p>${message}</p>`;
};

const handleAnswer = (selectedAnswer, question) => {
    console.log("Selected answer:", selectedAnswer, "Token:", question.token);

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
};

const resetTrivia = () => {
    disableControls(false);
    TriviaState.difficulty = "all";
    TriviaState.category = 0;
    TriviaState.questions = [];
    TriviaState.currentQuestionIndex = 0;

    document.querySelector(".trivia > div").innerHTML =
        "<h2>Trivia Questions</h2><p>Select your settings and then start!</p>";
};

const disableControls = (disable) => {
    document.querySelectorAll(".category .nav-button, .difficulty .nav-button, .header-btn")
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
