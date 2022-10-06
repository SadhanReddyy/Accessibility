package com.amazon.awsconsoleaccessibility.activity;

import java.util.ArrayList;
import java.util.List;

import com.amazon.awsconsoleaccessibility.Beer;
import com.amazon.awsconsoleaccessibility.BeerList;
import com.amazon.awsconsoleaccessibility.DependencyException;
import com.amazon.coral.annotation.Operation;
import com.amazon.coral.annotation.Service;
import com.amazon.coral.service.Activity;
import com.amazon.coral.service.LogRequests;
import com.amazon.coral.validate.ValidationException;

/**
 * 
 * Activity class for all Beer-related methods
 * 
 */

@Service("AWSConsoleAccessibility")
public class BeerActivity extends Activity {

    private static List<Beer> savedBeers = new ArrayList<Beer>();
    private static long nextBeerId = 1;

    /**
     * List all beers in the service
     * 
     * @return A list of beers
     * @throws DependencyException On any dependency error
     */
    @Operation("GetAllBeers") // define the Coral service operation
    /*
     * @LogRequests annotation is an opt-in mechanism and will log the
     * input and output parameters from your Coral model. If any of
     * these values are sensitive or confidential, you can either
     * remove the @LogRequests annotation or mark the data as sensitive:
     * https://w.amazon.com/bin/view/Coral/Model/XML/Traits#Sensitive
     */
    @LogRequests
    public BeerList getAllBeers() throws DependencyException
    {
        synchronized (BeerActivity.class) {
            BeerList beerList = new BeerList();
            beerList.setBeers(new ArrayList<Beer>(savedBeers));
            return beerList;
        }
    }

    /**
     * Given a beer object (without id) create a new Beer in the system
     * 
     * @param inBeer The beer information (without id) to populate
     * @return The created beer (with id)
     * @throws Exception On any error
     */
    @Operation("CreateBeer") // define the Coral service operation
    /*
     * @LogRequests annotation is an opt-in mechanism and will log the
     * input and output parameters from your Coral model. If any of
     * these values are sensitive or confidential, you can either
     * remove the @LogRequests annotation or mark the data as sensitive:
     * https://w.amazon.com/bin/view/Coral/Model/XML/Traits#Sensitive
     */
    @LogRequests
    public Beer createBeer(final Beer inBeer) throws Exception {
        // Check the name
        if (inBeer.getName() == null || inBeer.getName().equals("")) {
            throw new ValidationException("Invalid beer name!");
        }

        synchronized (BeerActivity.class) {
            inBeer.setBeerId(nextBeerId++);
            savedBeers.add(inBeer);
        }
        return inBeer;
    }


    /**
     * Clear out all BeerModels stored in the database. Used for testing.
     */
    public static void clearBeers() {
        synchronized (BeerActivity.class) {
            savedBeers.clear();
        }
    }
}
