<?php
include_once "class.database.php";

$db = Database::getInstance();
$mysqli = $db->getConnection(); 
$result = $mysqli->query("SELECT MAX(routeId) AS maxrouteId FROM AdminRoutes");
$row = mysqli_fetch_assoc($result); 
$data = $row['maxrouteId'];
$data = $data + 1;
echo json_encode($data);
exit();
?>