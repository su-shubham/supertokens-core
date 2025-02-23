/*
 *    Copyright (c) 2020, VRAI Labs and/or its affiliates. All rights reserved.
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

package io.supertokens.webserver.api.thirdparty;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.supertokens.Main;
import io.supertokens.authRecipe.UserPaginationToken;
import io.supertokens.pluginInterface.RECIPE_ID;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.thirdparty.ThirdParty;
import io.supertokens.thirdparty.UserPaginationContainer;
import io.supertokens.webserver.InputParser;
import io.supertokens.webserver.WebserverAPI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Deprecated
public class UsersAPI extends WebserverAPI {

    private static final long serialVersionUID = -2225750492558064634L;

    public UsersAPI(Main main) {
        super(main, RECIPE_ID.THIRD_PARTY.toString());
    }

    @Override
    public String getPath() {
        return "/recipe/users";
    }

    @Override
    @Deprecated
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        /*
         * pagination token can be null or string.
         * if string, it should be a base64 encoded JSON object.
         * pagination token will corresponds to the first item of the users' list.
         */
        String paginationToken = InputParser.getQueryParamOrThrowError(req, "paginationToken", true);
        /*
         * limit can be null or an integer with maximum value 1000.
         * default value will be 100.
         */
        Integer limit = InputParser.getIntQueryParamOrThrowError(req, "limit", true);
        /*
         * timeJoinedOrder can be null or string.
         * if not null, the value should be either "ASC" or "DESC".
         * default value will be "ASC"
         */
        String timeJoinedOrder = InputParser.getQueryParamOrThrowError(req, "timeJoinedOrder", true);

        if (timeJoinedOrder != null) {
            if (!timeJoinedOrder.equals("ASC") && !timeJoinedOrder.equals("DESC")) {
                throw new ServletException(new BadRequestException("timeJoinedOrder can be either ASC OR DESC"));
            }
        } else {
            timeJoinedOrder = "ASC";
        }

        if (limit != null) {
            if (limit > 1000) {
                throw new ServletException(new BadRequestException("max limit allowed is 1000"));
            } else if (limit < 1) {
                throw new ServletException(
                        new BadRequestException("limit must a positive integer with max value 1000"));
            }
        } else {
            limit = 100;
        }

        try {
            UserPaginationContainer users = ThirdParty.getUsers(super.main, paginationToken, limit, timeJoinedOrder);
            JsonObject result = new JsonObject();
            result.addProperty("status", "OK");
            JsonArray usersJson = new JsonParser().parse(new Gson().toJson(users.users)).getAsJsonArray();
            result.add("users", usersJson);
            if (users.nextPaginationToken != null) {
                result.addProperty("nextPaginationToken", users.nextPaginationToken);
            }
            super.sendJsonResponse(200, result, resp);
        } catch (UserPaginationToken.InvalidTokenException e) {
            throw new ServletException(new BadRequestException("invalid pagination token"));
        } catch (StorageQueryException e) {
            throw new ServletException(e);
        }
    }
}
