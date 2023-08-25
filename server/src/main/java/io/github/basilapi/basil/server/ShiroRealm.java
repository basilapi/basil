/*
 * Copyright (c) 2023. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.server;

import io.github.basilapi.basil.rdf.RDFFactory;
import io.github.basilapi.basil.store.tdb2.TDB2UserManager;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Set;

public class ShiroRealm extends AuthorizingRealm {
    private BasilConfiguration basilConfiguration;

    private TDB2UserManager userManager = null;

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) authenticationToken;
        String username = upToken.getUsername();
        // Null username is invalid
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }
        String password = null;
        password = getUserManager().getUser(username).getPassword();
        if (password == null) {
            throw new UnknownAccountException("No account found for user [" + username + "]");
        }
        return new SimpleAuthenticationInfo(username, password.toCharArray(), getName());
    }


    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //null usernames are invalid
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }

        String username = (String) getAvailablePrincipal(principals);
        Set<String> roleNames = getUserManager().getUserApis(username);
        return new SimpleAuthorizationInfo(roleNames);
    }

    public void setBasilConfiguration(BasilConfiguration conf){
        this.basilConfiguration = conf;
    }

    public BasilConfiguration getBasilConfiguration(){
        return basilConfiguration;
    }

    public TDB2UserManager getUserManager() {
        if(userManager == null){
            userManager = new TDB2UserManager(basilConfiguration.getTdbLocation(), new RDFFactory(basilConfiguration.getNamespace()));
        }
        return userManager;
    }
}
