package com.amazon.awsconsoleaccessibility.test;

import java.util.UUID;

import org.junit.Test;
import static org.junit.Assert.*;

import com.amazon.awsconsoleaccessibility.Beer;
import com.amazon.awsconsoleaccessibility.BeerList;

import org.junit.Before;
import com.amazon.awsconsoleaccessibility.activity.BeerActivity;

/**
 *
 * A collection of unit tests for the BeerActivity class. This set
 * tests each method of the BeerActivity class by using the Coral mock client
 * which will verify that the Coral layer, such as our interceptor, is being
 * called as if it were running in the server context.
 *
 */
public class BeerActivitiesTest extends AbstractTestCase {

    /**
     * Test for getAllBeers()
     * @throws Exception
     */
    @Test
    public void testGetAllBeers() throws Exception {
        // Create lots of beers, verify that all are accounted for
        Beer beers[] = createBeers(100);
        BeerList beerList = client.newGetAllBeersCall().call();
        assertEquals(beers.length, beerList.getBeers().size());
        for (Beer beer : beers) {
            assertBeerListContains(beerList, beer);
        }
    }

    /**
     * Test for createBeer()
     * @throws Exception
     */
    @Test
    public void testCreateBeer() throws Exception {
        // Create the beer via the activity class
        Beer beerToCreate = new Beer();
        beerToCreate.setName(getRandomString());
        beerToCreate.setDescription(getRandomString());
        beerToCreate.setBeerCompanyName(getRandomString());
        beerToCreate.setBeerTypeName(getRandomString());

        Beer createdBeer = client.newCreateBeerCall().call(beerToCreate);

        // Validate what we sent in is still there
        assert(createdBeer.getName().equals(beerToCreate.getName()));
        assert(createdBeer.getDescription().equals(beerToCreate.getDescription()));
        assert(createdBeer.getBeerCompanyName().equals(beerToCreate.getBeerCompanyName()));
        assert(createdBeer.getBeerTypeName().equals(beerToCreate.getBeerTypeName()));

        // Validate we got back an id
        assertNotNull(createdBeer.getBeerId());

        BeerList beerList = client.newGetAllBeersCall().call();
        assertBeerListContains(beerList, createdBeer);
    }

    /****
     *
     * Data validation methods
     *
     ****/

    /**
     * Checks the BeerList contains the given Beer.
     *
     * @param beerList The beer list to check
     * @param models The set of models to check
     */
    private void assertBeerListContains(BeerList beerList, Beer beer) {
        for (Beer listBeer : beerList.getBeers()) {
            if (listBeer.equals(beer)) {
                return;
            }
        }
        assertTrue("Expected beer was not found.", false);
    }

    /****
     *
     * DB population methods
     *
     ****/

    /**
     * Creates a new set of beer models
     *
     * @param amount The number of beers to create
     * @return The array of beers it created
     * @throws Exception
     */
    private Beer[] createBeers(int amount) throws Exception {
        Beer[] beers = new Beer[amount];

        for (int i = 0; i < amount; i++) {
            Beer beer = new Beer();
            beer.setName(i + getRandomString());
            beer.setDescription(getRandomString());
            beer.setBeerCompanyName(getRandomString());
            beer.setBeerTypeName(getRandomString());
            beers[i] = client.newCreateBeerCall().call(beer);
        }

        return beers;
    }

    /**
     * Returns a random string
     */
    protected String getRandomString() {
        return UUID.randomUUID().toString();
    }

    /**
     * Clear out beers from database's store
     */
    @Before
    public void clearBeers() throws Exception {

        BeerActivity.clearBeers();
    }
}