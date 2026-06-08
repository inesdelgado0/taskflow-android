const express = require("express");
const bcrypt = require("bcryptjs");
const path = require("path");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase } = require("../utils/http");
const { requireAuth } = require("../middleware/auth");
const { unixTimestampMs } = require("../utils/time");

const router = express.Router();
const BCRYPT_SALT_ROUNDS = Number(process.env.BCRYPT_SALT_ROUNDS || 12);
const PROFILE_PHOTO_BUCKET = process.env.SUPABASE_PROFILE_PHOTOS_BUCKET || "profile-photos";
const MAX_PROFILE_PHOTO_BYTES = Number(process.env.MAX_PROFILE_PHOTO_BYTES || 10 * 1024 * 1024);
let profilePhotoBucketReady = false;

function extensionFor(contentType) {
  if (contentType === "image/png") return ".png";
  if (contentType === "image/webp") return ".webp";
  if (contentType === "image/gif") return ".gif";
  return ".jpg";
}

async function ensureProfilePhotoBucket() {
  if (profilePhotoBucketReady) return;

  const { data: buckets, error: listError } = await supabase.storage.listBuckets();
  if (listError) throw listError;

  const exists = (buckets || []).some((bucket) => bucket.name === PROFILE_PHOTO_BUCKET);
  if (!exists) {
    const { error: createError } = await supabase.storage.createBucket(PROFILE_PHOTO_BUCKET, {
      public: true,
      fileSizeLimit: MAX_PROFILE_PHOTO_BYTES,
      allowedMimeTypes: ["image/jpeg", "image/png", "image/webp", "image/gif"]
    });
    if (createError) throw createError;
  }

  profilePhotoBucketReady = true;
}

function toUserResponse(user) {
  const roles = (user.user_roles || [])
    .map((entry) => entry.roles && entry.roles.code)
    .filter(Boolean);
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

router.get("/", asyncRoute(async (_req, res) => {
  const result = await supabase
    .from("users")
    .select(`
      id,
      name,
      username,
      email,
      photo_url,
      is_active,
      created_at,
      updated_at,
      user_roles (
        roles (
          code
        )
      )
    `)
    .order("name", { ascending: true });

  if (result.error) {
    return res.status(400).json({ message: result.error.message });
  }

  const users = (result.data || []).map(toUserResponse);

  return handleSupabase(res, { data: users, error: null });
}));

router.get("/me", requireAuth, asyncRoute(async (req, res) => {
  const userId = Number(req.user.sub);
  const result = await supabase
    .from("users")
    .select(`
      id,
      name,
      username,
      email,
      photo_url,
      is_active,
      created_at,
      updated_at,
      user_roles (
        roles (
          code
        )
      )
    `)
    .eq("id", userId)
    .maybeSingle();

  if (result.error) {
    return res.status(400).json({ message: result.error.message });
  }

  if (!result.data) {
    return res.status(404).json({ message: "User not found." });
  }

  return handleSupabase(res, { data: toUserResponse(result.data), error: null });
}));

router.put("/me", requireAuth, asyncRoute(async (req, res) => {
  const userId = Number(req.user.sub);
  const { name, username, email, photo_url: photoUrl, password } = req.body;

  if (!name || !username || !email) {
    return res.status(400).json({ message: "Name, username and email are required." });
  }

  const cleanedEmail = email.trim();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleanedEmail)) {
    return res.status(400).json({ message: "Invalid email." });
  }

  if (password && password.length < 8) {
    return res.status(400).json({ message: "Password must be at least 8 characters." });
  }

  const update = {
    name: name.trim(),
    username: username.trim(),
    email: cleanedEmail,
    photo_url: photoUrl || null,
    updated_at: unixTimestampMs()
  };

  if (password) {
    update.password_hash = await bcrypt.hash(password, BCRYPT_SALT_ROUNDS);
  }

  const result = await supabase
    .from("users")
    .update(update)
    .eq("id", userId)
    .select(`
      id,
      name,
      username,
      email,
      photo_url,
      is_active,
      created_at,
      updated_at,
      user_roles (
        roles (
          code
        )
      )
    `)
    .single();

  if (result.error) {
    return res.status(400).json({ message: result.error.message });
  }

  return handleSupabase(res, { data: toUserResponse(result.data), error: null });
}));

router.post(
  "/me/photo",
  requireAuth,
  express.raw({ type: ["image/jpeg", "image/png", "image/webp", "image/gif", "application/octet-stream"], limit: MAX_PROFILE_PHOTO_BYTES }),
  asyncRoute(async (req, res) => {
    const userId = Number(req.user.sub);
    const contentType = req.headers["content-type"] || "image/jpeg";

    if (!Buffer.isBuffer(req.body) || req.body.length === 0) {
      return res.status(400).json({ message: "Photo bytes are required." });
    }

    if (!contentType.startsWith("image/") && contentType !== "application/octet-stream") {
      return res.status(415).json({ message: "Only image uploads are supported." });
    }

    await ensureProfilePhotoBucket();

    const objectPath = path.posix.join(
      "users",
      String(userId),
      `profile_${Date.now()}${extensionFor(contentType)}`
    );

    const upload = await supabase.storage
      .from(PROFILE_PHOTO_BUCKET)
      .upload(objectPath, req.body, {
        contentType: contentType === "application/octet-stream" ? "image/jpeg" : contentType,
        upsert: true
      });

    if (upload.error) {
      return res.status(400).json({ message: upload.error.message });
    }

    const publicUrl = supabase.storage
      .from(PROFILE_PHOTO_BUCKET)
      .getPublicUrl(objectPath).data.publicUrl;

    const result = await supabase
      .from("users")
      .update({
        photo_url: publicUrl,
        updated_at: unixTimestampMs()
      })
      .eq("id", userId)
      .select(`
        id,
        name,
        username,
        email,
        photo_url,
        is_active,
        created_at,
        updated_at,
        user_roles (
          roles (
            code
          )
        )
      `)
      .single();

    if (result.error) {
      return res.status(400).json({ message: result.error.message });
    }

    return handleSupabase(res, { data: toUserResponse(result.data), error: null });
  })
);

module.exports = router;
