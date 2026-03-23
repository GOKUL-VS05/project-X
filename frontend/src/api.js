export const loginUser = async (email, password) => {
  if (email && password) {
    return { token: "mock-jwt-token", user: { email } };
  }

  throw new Error("Invalid credentials");
};
