package uk.ac.open.kmi.basil.swagger;

import com.wordnik.swagger.core.filter.SwaggerSpecFilter;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;

/**
 * Created by Luca Panziera on 14/03/15.
 */
public class CustomFilter implements SwaggerSpecFilter {
    @Override
    public boolean isOperationAllowed(
            Operation operation,
            ApiDescription api,
            java.util.Map<String, java.util.List<String>> params,
            java.util.Map<String, String> cookies,
            java.util.Map<String, java.util.List<String>> headers) {
        return true;
    }

    @Override
    public boolean isParamAllowed(
            Parameter parameter,
            Operation operation,
            ApiDescription api,
            java.util.Map<String, java.util.List<String>> params,
            java.util.Map<String, String> cookies,
            java.util.Map<String, java.util.List<String>>  headers) {
        return true;
    }
}
