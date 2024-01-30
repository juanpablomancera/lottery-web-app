/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lottery.user;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import lottery.pool.PoolService;
import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration testing class for
 * @see UserController
 * @author Konrad Löhr, Juan Pablo Mancera
 */

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTests {

	@Autowired MockMvc mvc;

	@Autowired
	UserController controller;

	@Autowired UserService userService;

	@Autowired
	UserAccountManagement userAccountManagement;

	@Autowired
	PoolService poolService;

	@Test
	void showsWelcomeMessage() throws Exception {
		mvc.perform(get("/login")) //
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Please sign in")));
	}

	@Test
	void rejectsUnauthenticatedAccessToController() {
		assertThatExceptionOfType(AuthenticationException.class) //
				.isThrownBy(() -> controller.adminPanel(new ExtendedModelMap(), mock(RegistrationForm.class)));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void allowsAuthenticatedAccessToController(){
		ExtendedModelMap model = new ExtendedModelMap();
		controller.adminPanel(model, mock(RegistrationForm.class));

		assertThat(model.get("userList")).isNotNull();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminPanelAccess() throws Exception {
		mvc.perform(get("/adminPanel")) //
				.andExpect(status().isOk()) //
				.andExpect(model().attributeExists("activeEvents"))
				.andExpect(model().attributeExists("inactiveEvents"))
				.andExpect(model().attributeExists("income"))
				.andExpect(model().attributeExists("expenses"))
				.andExpect(model().attributeExists("time"))
				.andExpect(model().attributeExists("userList"))
				.andExpect(model().attributeExists("lottobets"))
				.andExpect(model().attributeExists("totobets"));
	}

	@Test
	void registerUser(){
		RegistrationForm form = new RegistrationForm("test", "test@test", "test");
		Errors errors = mock(Errors.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		assertThat(controller.registerUser(form, errors, redirectAttributes)).isEqualTo("redirect:/login");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void registerAdmin(){
		RegistrationForm form = new RegistrationForm("newAdmin", "new@admin", "test");
		Errors errors = mock(Errors.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		controller.registerAdmin(form, errors, redirectAttributes);

		User user = userService.getUserByEmail("new@admin").orElse(null);

		assertThat(user).isNotNull();
		assertEquals(user.getUserAccount().getUsername(), "newAdmin");
		assertThat(controller.registerAdmin(form,errors,redirectAttributes)).isEqualTo("redirect:/adminPanel");
	}

	@Test
	void unsuccessfulRegisterUser(){
		RegistrationForm form = new RegistrationForm("peter", "test@test", "test");
		Errors errors = mock(Errors.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		assertThat(controller.registerUser(form, errors, redirectAttributes)).isEqualTo("redirect:/register");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void unsuccessfulRegisterAdmin(){
		RegistrationForm form = new RegistrationForm("admin", "newAdmin@test", "test");
		Errors errors = mock(Errors.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		controller.registerUser(form, errors, redirectAttributes);
		User user = userService.getUserByEmail("newAdmin@test").orElse(null);

		assertThat(controller.registerAdmin(form,errors,redirectAttributes)).isEqualTo("redirect:/adminPanel");
		assertNull(user);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteUser(){
		ExtendedModelMap model = new ExtendedModelMap();

		User user = new User(userAccountManagement.create(
				"delete", Password.UnencryptedPassword.of("test"), "delete@delete", Role.of("USER")));
		userService.saveUser(user);
		Long id = userService.getUserByEmail("delete@delete").orElseThrow().getId();
		Optional<UserAccount> userAccount = Optional.empty();

		controller.deleteUser(userAccount, id, model, mock(RedirectAttributes.class));
		assertThat(userService.getUserById(id)).isEmpty();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void isNotPossibleToDeleteHimself(){
		List<User> users = userService.getAllUsers();

		for(User user : users){
			Optional<UserAccount> userAccount = Optional.ofNullable(user.getUserAccount());
			controller.deleteUser(userAccount, user.getId(), mock(ExtendedModelMap.class), mock(RedirectAttributes.class));
		}
		List<User> admins = userService.getAllUsers().stream().filter(user ->
				user.getUserAccount().hasRole(Role.of("ADMIN"))).toList();

		assertEquals(admins.size(), 4);
	}

	@Test
	void login(){
		assertThat(controller.login(new ExtendedModelMap())).isEqualTo("login");
	}

	@Test
	void register(){
		assertThat(controller.register(new ExtendedModelMap(), mock(RegistrationForm.class))).isEqualTo("register");
	}

	@Test
	@WithMockUser
	void accountOfCurrentUser(){
		ExtendedModelMap model = new ExtendedModelMap();

		User user = new User(userAccountManagement.create(
			"loggedIn", Password.UnencryptedPassword.of("test"), "log@log", Role.of("USER")));
		userService.saveUser(user);

		assertThat(controller.accountOfCurrentUser(model, Optional.ofNullable(user.getUserAccount()))).isEqualTo("account");

	}

	@Test
	@WithMockUser
	void currentUserName(){
		User user = new User(userAccountManagement.create(
			"loggedIn2", Password.UnencryptedPassword.of("test"), "log2@log", Role.of("USER")));
		userService.saveUser(user);

		assertThat(controller.currentUserName(Optional.ofNullable(user.getUserAccount()))).isEqualTo(user.getUserAccount().getUsername());

	}

	@Test
	@WithMockUser
	void currentUserId(){
		User user = new User(userAccountManagement.create(
			"loggedIn3", Password.UnencryptedPassword.of("test"), "log3@log", Role.of("USER")));
		userService.saveUser(user);

		assertThat(controller.currentUserId(Optional.ofNullable(user.getUserAccount()))).isEqualTo(user.getId());

	}


	@Test
	@WithMockUser(roles = "ADMIN")
	void userDetails() throws Exception {
		User user = new User(userAccountManagement.create(
			"loggedIn5", Password.UnencryptedPassword.of("test"), "log5@log", Role.of("USER")));
		userService.saveUser(user);
		long userId = user.getId();
		mvc.perform(get("/userDetails/"+ userId)) //
			.andExpect(status().isOk()) //
			.andExpect(model().attributeExists("bets"))
			.andExpect(model().attributeExists("pools"))
			.andExpect(model().attributeExists("accountBalance"))
			.andExpect(model().attributeExists("user"));

	}

	@Test
	@WithMockUser
	void depositMoney(){
		User user = new User(userAccountManagement.create(
			"loggedIn6", Password.UnencryptedPassword.of("test"), "log6@log", Role.of("USER")));
		userService.saveUser(user);

		double amount = 1.0;
		double accountBalance = user.getAccountState();
		controller.depositMoney(Optional.of(user.getUserAccount()),amount);

		assertThat(accountBalance  + amount)
			.isEqualTo(userService.getUserById(user.getId()).orElseThrow().getAccountState());
		assertThat(controller.depositMoney(Optional.of(user.getUserAccount()),amount))
			.isEqualTo("redirect:/account");
	}

	@Test
	@WithMockUser
	void withdrawMoney(){
		User user = new User(userAccountManagement.create(
			"loggedIn8", Password.UnencryptedPassword.of("test"), "log8@log", Role.of("USER")));
		userService.saveUser(user);

		double amount = 1.0;
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

		controller.depositMoney(Optional.of(user.getUserAccount()),amount * 2);
		double accountBalance = userService.getUserById(user.getId()).orElseThrow().getAccountState();

		controller.withdrawMoney(Optional.of(user.getUserAccount()), amount, redirectAttributes);
		assertThat(accountBalance  - amount)
			.isEqualTo(userService.getUserById(user.getId()).orElseThrow().getAccountState());
		assertThat(controller.withdrawMoney(Optional.of(user.getUserAccount()),amount, redirectAttributes))
			.isEqualTo("redirect:/account");
	}



	@Test
	@WithMockUser(roles = "ADMIN")
	void withdrawMoneyFromUser(){
		User user = new User(userAccountManagement.create(
			"loggedIn7", Password.UnencryptedPassword.of("test"), "log7@log", Role.of("USER")));
		userService.saveUser(user);
		User admin = new User(userAccountManagement.create(
			"loggedIn9", Password.UnencryptedPassword.of("test"), "log9@log", Role.of("ADMIN")));
		userService.saveUser(admin);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		double amount = 1.0;
		user.addMoney(amount * 2);
		userService.saveUser(user);

		//no change because to high withdrawl
		controller.withdrawMoneyFromUser(user.getId(), amount * 4, redirectAttributes);
		assertThat(userService.getUserById(user.getId()).orElseThrow().getAccountState()).isEqualTo(amount*2);

		controller.withdrawMoneyFromUser(user.getId(), amount, redirectAttributes);
		assertThat(userService.getUserById(user.getId()).orElseThrow().getAccountState()).isEqualTo(amount);
		//no change because admin can't have money
		controller.withdrawMoneyFromUser(admin.getId(), amount, redirectAttributes);
		assertThat(userService.getUserById(admin.getId()).orElseThrow().getAccountState()).isEqualTo(0.0);

		assertThat(controller.withdrawMoneyFromUser(user.getId(), amount, redirectAttributes)).isEqualTo("redirect:/userDetails/" + user.getId());
		assertThat(controller.withdrawMoneyFromUser(admin.getId(), amount, redirectAttributes)).isEqualTo("redirect:/userDetails/" + admin.getId());
	}
}
