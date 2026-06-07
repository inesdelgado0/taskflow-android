const USER_RELATION_SELECT = `
  user_roles (
    roles (
      code
    )
  )
`;

const USER_SELECT = `
  id,
  name,
  username,
  email,
  photo_url,
  is_active,
  created_at,
  updated_at,
  ${USER_RELATION_SELECT}
`;

function extractRoleCodes(user) {
  return (user.user_roles || [])
    .map((entry) => entry.roles && entry.roles.code)
    .filter(Boolean);
}

function toUserResponse(user) {
  const roles = extractRoleCodes(user);
  const normalizedRoles = roles.length > 0 ? roles : ["USER"];

  return {
    id: user.id,
    name: user.name,
    username: user.username,
    email: user.email,
    role: normalizedRoles[0],
    roles: normalizedRoles,
    photo_url: user.photo_url,
    is_active: user.is_active,
    created_at: user.created_at,
    updated_at: user.updated_at
  };
}

module.exports = {
  USER_RELATION_SELECT,
  USER_SELECT,
  extractRoleCodes,
  toUserResponse
};
