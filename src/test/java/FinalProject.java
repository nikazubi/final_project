import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import org.openqa.selenium.By;
import org.testng.annotations.Test;


import java.util.List;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;

public class FinalProject {

    @Test
    public void test1() {
        String username = "nikazubi";
        String password = "Nika1234!";
        JSONObject userInfo = new JSONObject();
        userInfo.put("userName", username);
        userInfo.put("password", password);

        Response response = sendPost("https://bookstore.toolsqa.com/Account/v1/User", userInfo.toJSONString());
        Assert.assertEquals(response.statusCode(), 201);

        open("https://demoqa.com/login");

        $("#userName").sendKeys(username);
        $("#password").sendKeys(password);
        $("#login").click();

        FindAndScroll($(By.xpath("//button[text()='Delete Account']"))).click();
        $("#closeSmallModal-ok").click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Alert alert = getWebDriver().switchTo().alert();
        Assert.assertEquals(alert.getText(), "User Deleted.");
        alert.accept();

        $("#userName").sendKeys(username);
        $("#password").sendKeys(password);
        $("#login").click();

        $("#name").shouldBe(Condition.visible).shouldHave(Condition.text("Invalid username or password!"));

        JSONObject login = new JSONObject();
        login.put("userName", username);
        login.put("password", password);
        Response rawResponse = sendPost("https://bookstore.toolsqa.com/Account/v1/Authorized", login.toJSONString());

        Assert.assertEquals(rawResponse.getBody().jsonPath().getString("message"), "User not found!");
    }

    @Test
    public void test2() {
        open("https://demoqa.com/books");
        $("#searchBox").sendKeys("O'Reilly Media");

        WebDriverManager.chromedriver().setup();
        ChromeOptions chrome = new ChromeOptions();
        ChromeDriver driver = new ChromeDriver(chrome);
        List<WebElement> elements = driver.findElements(By.xpath("//div[@class='rt-td' and contains(text(),'Reilly Media')]"));
        long sizeOfReilly = elements.stream().count();

        String stringOfBookss = sendGet("https://bookstore.toolsqa.com/BookStore/v1/Books").getBody().asString();
        int sizeOfBooks = StringUtils.countMatches(stringOfBookss, "O'Reilly Media");
        System.out.println(sizeOfBooks + " " + sizeOfReilly);
        assert elements.stream().count() == sizeOfReilly;
    }

    public Response sendPost(String url, Object payload) {
        RequestSpecification spec = given()
                .header("Content-type", "application/json")
                .body(payload)
                .when();

        return spec.post(url).then().extract().response();
    }

    public static Response sendGet(String url) {
        return given().contentType(ContentType.JSON).when().get(url);
    }

    public static SelenideElement FindAndScroll(SelenideElement element) {
        return element.scrollIntoView("{behavior: \"instant\", block: \"center\", inline: \"center\"}");
    }
}