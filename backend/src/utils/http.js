function sendData(res, data, status = 200) {
  return res.status(status).json(data);
}

function sendNoContent(res) {
  return res.status(204).send();
}

function handleSupabase(res, result, notFoundMessage = "Resource not found.") {
  if (result.error) {
    return res.status(400).json({ message: result.error.message });
  }

  if (result.data === null || typeof result.data === "undefined") {
    return res.status(404).json({ message: notFoundMessage });
  }

  return sendData(res, result.data);
}

function asyncRoute(handler) {
  return (req, res, next) => {
    Promise.resolve(handler(req, res, next)).catch(next);
  };
}

module.exports = {
  asyncRoute,
  handleSupabase,
  sendData,
  sendNoContent
};
