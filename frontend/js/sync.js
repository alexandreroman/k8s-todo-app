function syncEntries() {
  console.log("Synchronizing todo entries");
  axios.get(API + "/api/todos").then(res => {
    app.todos = res.data;
  });
};

syncEntries();
