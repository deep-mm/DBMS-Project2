package src.plan;

import src.tx.Transaction;
import src.parse.QueryData;

/**
 * The interface implemented by planners for 
 * the SQL select statement.
 * @author Edward Sciore
 *
 */
public interface QueryPlanner {
   
   /**
    * Creates a plan for the parsed query.
    * @param data the parsed representation of the query
    * @param tx the calling transaction
    * @return a plan for that query
    */
   public Plan createPlan(QueryData data, Transaction tx);
}
