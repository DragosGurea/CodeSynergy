$mvnArgs = @(
  "compile", "exec:java",
  "-Dexec.mainClass=com.groupama.document.detection.app.Main",
  '-Dexec.args=--runner=DataflowRunner --project=prj-hackathon-team4 --region=europe-west1 --jobName=document-processing-v1 --network=projects/prj-hackathon-team4/global/networks/dataflow-vpc --subnetwork=regions/europe-west1/subnetworks/dataflow-subnet --tempLocation=gs://dataflow-job-document-ai/temp --stagingLocation=gs://dataflow-job-document-ai/staging --configFile=config/app.conf --serviceAccount=530646005040-compute@developer.gserviceaccount.com'
)

mvn @mvnArgs