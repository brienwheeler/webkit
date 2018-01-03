/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2018 Brien L. Wheeler (brienwheeler@yahoo.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.brienwheeler.svc.users;

import com.brienwheeler.lib.db.domain.DbId;
import com.brienwheeler.lib.svc.IStoppableService;
import com.brienwheeler.svc.users.domain.User;

/**
 * A service for managing User records.
 *  
 * @author bwheeler
 */
public interface IUserService extends IStoppableService
{
	/**
	 * Create a new User object after checking to ensure that one with the requested
	 * username doesn't already exist.
	 * 
	 * @param username username for the new User
	 * @param hashedPassword password for the new User (already hashed for security)
	 * @param callbacks objects to be called back with the saved User object before
	 * 		the transaction is committed
	 * @return the created and persisted User object
	 * @throws DuplicateUserException if a User with the requested username already exists
	 * 		in the database
	 */
	User createUser(String username, String hashedPassword,
			CreateUserCallback... callbacks) throws DuplicateUserException;
	
	/**
	 * Find the User record for the given id
	 * @param userId the user id to look up
	 * @return the User record corresponding to the given user id, or null if one does not
	 * 		exist
	 */
	User findById(DbId<User> userId);

	/**
	 * Find the User record for the given username
	 * @param username the username to look up
	 * @return the User record corresponding to the given username, or null if one does not
	 * 		exist
	 */
	User findByUsername(String username);
	
	void setNewPassword(User user, String newHashedPassword);
	
	/**
	 * This interface allows a consumer of {@link IUserService#createUser(String, String, CreateUserCallback...)}
	 * to receive callbacks after the User object has been saved but before the encompassing transaction has
	 * been committed.
	 */
	interface CreateUserCallback {
		void userCreated(User user);
	}
}
