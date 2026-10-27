const reportsDatabase = db.getSiblingDB("gym_reports");

if (!reportsDatabase.getCollectionNames().includes("trainings_duration_summary")) {
    reportsDatabase.createCollection("trainings_duration_summary");
}

reportsDatabase.trainings_duration_summary.createIndex(
    { trainerUsername: 1 },
    { unique: true, name: "uk_trainer_username" }
);
