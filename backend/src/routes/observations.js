const express = require("express");
const path = require("path");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase, sendNoContent } = require("../utils/http");
const { unixTimestampMs } = require("../utils/time");

const router = express.Router();
const PHOTO_BUCKET = process.env.SUPABASE_OBSERVATION_PHOTOS_BUCKET || "observation-photos";
const MAX_PHOTO_BYTES = Number(process.env.MAX_OBSERVATION_PHOTO_BYTES || 10 * 1024 * 1024);
let storageBucketReady = false;

function extensionFor(contentType) {
  if (contentType === "image/png") return ".png";
  if (contentType === "image/webp") return ".webp";
  if (contentType === "image/gif") return ".gif";
  return ".jpg";
}

async function ensurePhotoBucket() {
  if (storageBucketReady) return;

  const { data: buckets, error: listError } = await supabase.storage.listBuckets();
  if (listError) throw listError;

  const exists = (buckets || []).some((bucket) => bucket.name === PHOTO_BUCKET);
  if (!exists) {
    const { error: createError } = await supabase.storage.createBucket(PHOTO_BUCKET, {
      public: true,
      fileSizeLimit: MAX_PHOTO_BYTES,
      allowedMimeTypes: ["image/jpeg", "image/png", "image/webp", "image/gif"]
    });
    if (createError) throw createError;
  }

  storageBucketReady = true;
}

router.get("/tasks/:taskId/observations", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("observations")
    .select("*")
    .eq("task_id", Number(req.params.taskId))
    .order("created_at", { ascending: false });

  return handleSupabase(res, result);
}));

router.post("/tasks/:taskId/observations", asyncRoute(async (req, res) => {
  const text = req.body.text || null;
  const photoPath = req.body.photo_path || null;

  if (!req.body.user_id || (!text && !photoPath)) {
    return res.status(400).json({ message: "user_id and text or photo_path are required." });
  }

  const result = await supabase
    .from("observations")
    .insert({
      task_id: Number(req.params.taskId),
      user_id: req.body.user_id,
      text,
      photo_path: photoPath,
      created_at: unixTimestampMs()
    })
    .select()
    .single();

  return handleSupabase(res, result);
}));

router.post(
  "/observations/:id/photo",
  express.raw({ type: ["image/jpeg", "image/png", "image/webp", "image/gif", "application/octet-stream"], limit: MAX_PHOTO_BYTES }),
  asyncRoute(async (req, res) => {
    const observationId = Number(req.params.id);
    const contentType = req.headers["content-type"] || "image/jpeg";

    if (!Buffer.isBuffer(req.body) || req.body.length === 0) {
      return res.status(400).json({ message: "Photo bytes are required." });
    }

    if (!contentType.startsWith("image/") && contentType !== "application/octet-stream") {
      return res.status(415).json({ message: "Only image uploads are supported." });
    }

    const existing = await supabase
      .from("observations")
      .select("id")
      .eq("id", observationId)
      .maybeSingle();

    if (existing.error) {
      return res.status(400).json({ message: existing.error.message });
    }

    if (!existing.data) {
      return res.status(404).json({ message: "Observation not found." });
    }

    await ensurePhotoBucket();

    const objectPath = path.posix.join(
      "observations",
      String(observationId),
      `${Date.now()}${extensionFor(contentType)}`
    );

    const upload = await supabase.storage
      .from(PHOTO_BUCKET)
      .upload(objectPath, req.body, {
        contentType: contentType === "application/octet-stream" ? "image/jpeg" : contentType,
        upsert: true
      });

    if (upload.error) {
      return res.status(400).json({ message: upload.error.message });
    }

    const publicUrl = supabase.storage
      .from(PHOTO_BUCKET)
      .getPublicUrl(objectPath).data.publicUrl;

    const result = await supabase
      .from("observations")
      .update({ photo_path: publicUrl })
      .eq("id", observationId)
      .select()
      .single();

    return handleSupabase(res, result);
  })
);

router.delete("/observations/:id", asyncRoute(async (req, res) => {
  const { error } = await supabase
    .from("observations")
    .delete()
    .eq("id", Number(req.params.id));

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return sendNoContent(res);
}));

module.exports = router;
