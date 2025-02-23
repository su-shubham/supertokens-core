/*
 *    Copyright (c) 2021, VRAI Labs and/or its affiliates. All rights reserved.
 *
 *    This software is licensed under the Apache License, Version 2.0 (the
 *    "License") as published by the Apache Software Foundation.
 *
 *    You may not use this file except in compliance with the License. You may
 *    obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */

package io.supertokens.webserver.api.core;

import com.google.gson.JsonObject;
import io.supertokens.Main;
import io.supertokens.authRecipe.AuthRecipe;
import io.supertokens.pluginInterface.RECIPE_ID;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.webserver.InputParser;
import io.supertokens.webserver.WebserverAPI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;

public class UsersCountAPI extends WebserverAPI {

    private static final long serialVersionUID = -2225750492558064634L;

    public UsersCountAPI(Main main) {
        super(main, "");
    }

    @Override
    public String getPath() {
        return "/users/count";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String[] recipeIds = InputParser.getCommaSeparatedStringArrayQueryParamOrThrowError(req, "includeRecipeIds",
                true);

        Stream.Builder<RECIPE_ID> recipeIdsEnumBuilder = Stream.<RECIPE_ID>builder();

        if (recipeIds != null) {
            for (String recipeId : recipeIds) {
                RECIPE_ID recipeID = RECIPE_ID.getEnumFromString(recipeId);
                if (recipeID == null) {
                    throw new ServletException(new BadRequestException("Unknown recipe ID: " + recipeId));
                }
                recipeIdsEnumBuilder.add(recipeID);
            }
        }

        try {
            long count = AuthRecipe.getUsersCount(super.main, recipeIdsEnumBuilder.build().toArray(RECIPE_ID[]::new));
            JsonObject result = new JsonObject();
            result.addProperty("status", "OK");
            result.addProperty("count", count);
            super.sendJsonResponse(200, result, resp);
        } catch (StorageQueryException e) {
            throw new ServletException(e);
        }
    }
}
