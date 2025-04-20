$(document).ready(function () {
    const apiKey = '974e06452eec49ca80e60844252002';
    const city = 'Red Deer';
    const apiUrl = `https://api.weatherapi.com/v1/current.json?key=${apiKey}&q=${city}&lang=en`;

    // This is  to Initialize night mode from localStorage if set
    if (localStorage.getItem('nightMode') === 'true') {
        $("body").addClass("night-mode");
        const footer = $("footer");
        footer.css("backgroundColor", "#333");
        $("#toggleDayNight").html(`<i id="toggleIcon" class="bi bi-sun-fill"></i>`);
    }

    // To Fetch weather data
    function getWeather() {
        $.getJSON(apiUrl)
            .done(function (data) {
                const temperature = data.current.temp_c;
                const condition = data.current.condition.text;
                $("#weather").html(`City: Red Deer 🌡️ ${temperature}°C | ${condition}`);
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                $("#weather").html("❌ Unable to fetch weather data.");
                console.error('Error fetching weather data:', textStatus, errorThrown);
            });
    }

    // This is to Auto-collapse navbar on item click
    $(".navbar-nav .dropdown-item").click(function () {
        $(".navbar-collapse").collapse('hide');
    });

    // This is to Toggle day/night mode & adjust footer color
    $("#toggleDayNight").click(function () {
        $("body").toggleClass("night-mode");
        const footer = $("footer");
        let icon = $("#toggleIcon");

        if ($("body").hasClass("night-mode")) {
            icon.removeClass("bi-moon-fill").addClass("bi-sun-fill");
            $(this).html(`<i id="toggleIcon" class="bi bi-sun-fill"></i>`);
            footer.css("backgroundColor", "#333");
            localStorage.setItem('nightMode', 'true');
        } else {
            icon.removeClass("bi-sun-fill").addClass("bi-moon-fill");
            $(this).html(`<i id="toggleIcon" class="bi bi-moon-fill"></i>`);
            footer.css("backgroundColor", "#ff758c");
            localStorage.setItem('nightMode', 'false');
        }
    });

    // To Fetch a random fun fact
    function fetchFunFact() {
        $.ajax({
            url: "https://uselessfacts.jsph.pl/random.json?language=en",
            method: "GET",
            success: function (data) {
                $("#fun-fact").text(data.text);
            },
            error: function () {
                $("#fun-fact").text("Failed to load fun fact. Try again later.");
            }
        });
    }
    $("#new-fact").click(fetchFunFact);
    fetchFunFact();

    // This is to Fetch a healthy recipe
    function fetchRecipe() {
        $.ajax({
            url: "https://www.themealdb.com/api/json/v1/1/random.php",
            method: "GET",
            success: function (data) {
                $("#recipe").html(`<strong>${data.meals[0].strMeal}</strong>`);
                $("#recipe-img").attr("src", data.meals[0].strMealThumb);
            },
            error: function () {
                $("#recipe").html("Failed to load recipe. Try again.");
                $("#recipe-img").attr("src", "IMAGE/recipe.jpg");
            }
        });
    }
    fetchRecipe();
    setInterval(fetchFunFact, 10000);
    setInterval(fetchRecipe, 10000);

    // For the Mood selector
    $("#mood-selector").change(function () {
        const mood = $(this).val();
        let response = "";
        let moodImage = "";

        if (mood === "happy") {
            response = "Great! Keep up the positive energy!";
            moodImage = "IMAGES/happy.jpg";
        } else if (mood === "neutral") {
            response = "Stay balanced and take a deep breath.";
            moodImage = "IMAGES/neutral.jpg";
        } else if (mood === "stressed") {
            response = "Take a break, relax, and recharge.";
            moodImage = "IMAGES/stressed.jpg";
        }

        $("#mood-response").text(response);
        $("#mood-img").attr("src", moodImage);
    });
    
    // for the Timer functionality
    let timer;
    let timeLeft = 1500; // 25 minutes
    let isRunning = false;
    
    // This is to Hide progress bar initially
    $("#progress-bar-container").css("opacity", "0");
    
    // This is to Show progress bar on hover or when timer is running
    $("#timer-container").hover(
        function() {
            $("#progress-bar-container").css("opacity", "1");
        },
        function() {
            if (!isRunning) {
                $("#progress-bar-container").css("opacity", "0");
            }
        }
    );

    function updateTimerDisplay() {
        let minutes = Math.floor(timeLeft / 60);
        let seconds = timeLeft % 60;
        $("#timer-display").text(
            `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`
        );
        
        let progressValue = ((1500 - timeLeft) / 1500) * 100;
        $("#progress-bar").css("width", progressValue + "%");
    }

    function startTimer() {
        if (!isRunning) {
            isRunning = true;
            $("#progress-bar-container").css("opacity", "1");
            timer = setInterval(() => {
                if (timeLeft > 0) {
                    timeLeft--;
                    updateTimerDisplay();
                } else {
                    clearInterval(timer);
                    isRunning = false;
                    $("#progress-bar-container").css("opacity", "0");
                    alert("Time's up!");
                }
            }, 1000);
        }
    }

    function pauseTimer() {
        clearInterval(timer);
        isRunning = false;
        if (!$("#timer-container").is(":hover")) {
            $("#progress-bar-container").css("opacity", "0");
        }
    }

    function resetTimer() {
        clearInterval(timer);
        isRunning = false;
        timeLeft = 1500;
        updateTimerDisplay();
        
        //To Shake and explode animation
        const progressBar = $("#progress-bar");
        const container = $("#progress-bar-container");
        
        container.css("opacity", "1");
        progressBar.addClass("shake");
        container.addClass("shake-container");
        
        setTimeout(() => {
            progressBar.removeClass("shake");
            container.removeClass("shake-container");
            progressBar.addClass("explode");
            
            setTimeout(() => {
                progressBar.removeClass("explode");
                if (!$("#timer-container").is(":hover")) {
                    container.css("opacity", "0");
                }
                // This is to Reset progress bar width
                $("#progress-bar").css("width", "0%");
            }, 500);
        }, 500);
    }

    // To Attach event listeners to timer buttons
    $("#start-timer").on("click", startTimer);
    $("#pause-timer").on("click", pauseTimer);
    $("#reset-timer").on("click", resetTimer);
    
    updateTimerDisplay();
    getWeather();


    $(document).ready(function() {
        // To Initialize the accordion
        $("#accordion").accordion({
          header: ".card-header",      
          collapsible: true,           
          active: false,               
          heightStyle: "content"       
        });
        
        // Optional: Toggle icon classes on click
        $(".accordion-toggle").click(function() {
          var icon = $(this).find("span");
          if (icon.hasClass("ui-icon-triangle-1-e")) {
            icon.removeClass("ui-icon-triangle-1-e").addClass("ui-icon-triangle-1-s");
          } else {
            icon.removeClass("ui-icon-triangle-1-s").addClass("ui-icon-triangle-1-e");
          }
        });
      });
});