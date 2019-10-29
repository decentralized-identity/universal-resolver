package uniresolver.examples;
import uniresolver.driver.did.work.DIDWorkDriver;
import uniresolver.result.ResolveResult;

public class TestDriverDidWork {

    public static void main(String[] args) throws Exception {

        DIDWorkDriver driver = new DIDWorkDriver();
        driver.setWorkDomain("https://credentials.id.workday.com");
        driver.setApiKey("sxVQUoDE015VhAs5ep4b57DFA5vT3zqvf1Dm1sGe");

        ResolveResult ResolveResult = driver.resolve("did:work:2UUHQCd4psvkPLZGnWY33L");
        System.out.println(ResolveResult.toJson());
    }
}
